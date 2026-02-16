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

/**
 * Sends SMS through a locally hosted gateway service.
 * Expected request body format:
 * {
 *   "to": "+639xxxxxxxxx",
 *   "message": "..."
 * }
 */
public class LocalSmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalSmsSender.class);

    private final String sendUrl;
    private final String authorizationValue;
    private final boolean dryRun;
    private final HttpClient httpClient;

    public LocalSmsSender(String sendUrl, String authorizationValue, boolean dryRun) {
        this.sendUrl = sendUrl;
        this.authorizationValue = authorizationValue;
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
            "{\"to\":\"%s\",\"message\":\"%s\"}",
            escapeJson(message.getTo()),
            escapeJson(message.getBody())
        );

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(sendUrl))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload));

        if (authorizationValue != null && !authorizationValue.isBlank()) {
            builder.header("Authorization", authorizationValue);
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                    "Local SMS send failed with status " + response.statusCode() + ": " + summarizeBody(response.body())
                );
            }
            LOGGER.info("Local SMS gateway accepted message for {}", message.getTo());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Local SMS call interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to call local SMS gateway", e);
        }
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
