package com.ev12.config.service;

import com.ev12.config.model.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PhilSmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhilSmsSender.class);

    private final String baseUrl;
    private final String apiToken;
    private final String senderId;
    private final String messagesPath;
    private final boolean dryRun;
    private final HttpClient httpClient;

    public PhilSmsSender(String baseUrl, String apiToken, String senderId, String messagesPath, boolean dryRun) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.senderId = senderId;
        this.messagesPath = messagesPath;
        this.dryRun = dryRun;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void send(List<SmsMessage> messages) {
        if (dryRun) {
            messages.forEach(message -> LOGGER.info("[DRY RUN] SMS to {}: {}", message.getTo(), message.getBody()));
            return;
        }

        for (SmsMessage message : messages) {
            sendOne(message);
        }
    }

    private void sendOne(SmsMessage message) {
        String payload = String.format(
            "{\"recipient\":\"%s\",\"sender_id\":\"%s\",\"message\":\"%s\"}",
            escapeJson(message.getTo()),
            escapeJson(senderId),
            escapeJson(message.getBody())
        );

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(trimTrailingSlash(baseUrl) + normalizePath(messagesPath)))
            .header("Authorization", "Bearer " + apiToken)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                    "PhilSMS send failed with status " + response.statusCode() + ": " + summarizeBody(response.body())
                );
            }
            LOGGER.info("PhilSMS accepted message for {}", message.getTo());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("PhilSMS call interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call PhilSMS API", e);
        }
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String normalizePath(String value) {
        if (value == null || value.isBlank()) {
            return "/messages";
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private String summarizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "<empty body>";
        }
        String oneLine = body.replaceAll("\\s+", " ").trim();
        if (oneLine.length() > 220) {
            return oneLine.substring(0, 220) + "...";
        }
        return oneLine;
    }

    private String escapeJson(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }
}
