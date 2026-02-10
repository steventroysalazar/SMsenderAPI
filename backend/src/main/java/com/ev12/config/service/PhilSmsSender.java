package com.ev12.config.service;

import com.ev12.config.model.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PhilSmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhilSmsSender.class);

    private final String baseUrl;
    private final String apiToken;
    private final String senderId;
    private final String messagesPath;
    private final String sendUrl;
    private final String contentType;
    private final boolean includeTokenInBody;
    private final boolean dryRun;
    private final HttpClient httpClient;

    public PhilSmsSender(
        String baseUrl,
        String apiToken,
        String senderId,
        String messagesPath,
        String sendUrl,
        String contentType,
        boolean includeTokenInBody,
        boolean dryRun
    ) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.senderId = senderId;
        this.messagesPath = messagesPath;
        this.sendUrl = sendUrl;
        this.contentType = contentType;
        this.includeTokenInBody = includeTokenInBody;
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
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(resolveSendUrl()))
            .header("Content-Type", normalizeContentType(contentType))
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(buildPayload(message)))
            .build();

        if (!includeTokenInBody && apiToken != null && !apiToken.isBlank()) {
            request = addAuthorization(request, apiToken);
        }

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

    private HttpRequest addAuthorization(HttpRequest request, String token) {
        return HttpRequest.newBuilder(request.uri())
            .headers(copyHeaders(request))
            .header("Authorization", "Bearer " + token)
            .method(request.method(), request.bodyPublisher().orElse(HttpRequest.BodyPublishers.noBody()))
            .build();
    }

    private String[] copyHeaders(HttpRequest request) {
        return request.headers().map().entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(value -> new String[]{entry.getKey(), value}))
            .flatMap(pair -> java.util.stream.Stream.of(pair[0], pair[1]))
            .toArray(String[]::new);
    }

    private String buildPayload(SmsMessage message) {
        if (normalizeContentType(contentType).contains("application/x-www-form-urlencoded")) {
            Map<String, String> form = new LinkedHashMap<>();
            form.put("recipient", message.getTo());
            form.put("sender_id", senderId);
            form.put("message", message.getBody());
            if (includeTokenInBody && apiToken != null && !apiToken.isBlank()) {
                form.put("api_token", apiToken);
            }
            return form.entrySet().stream()
                .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));
        }

        StringBuilder json = new StringBuilder()
            .append("{\"recipient\":\"").append(escapeJson(message.getTo())).append("\",")
            .append("\"sender_id\":\"").append(escapeJson(senderId)).append("\",")
            .append("\"message\":\"").append(escapeJson(message.getBody())).append("\"");

        if (includeTokenInBody && apiToken != null && !apiToken.isBlank()) {
            json.append(",\"api_token\":\"").append(escapeJson(apiToken)).append("\"");
        }

        json.append("}");
        return json.toString();
    }

    private String resolveSendUrl() {
        if (sendUrl != null && !sendUrl.isBlank()) {
            return sendUrl;
        }
        return trimTrailingSlash(baseUrl) + normalizePath(messagesPath);
    }

    private String normalizeContentType(String value) {
        if (value == null || value.isBlank()) {
            return "application/json";
        }
        return value;
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

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
