package com.ev12.config.service;

import com.ev12.config.model.InboundMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class LocalInboundMessageClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalInboundMessageClient.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LocalInboundMessageClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public List<InboundMessage> fetchMessages(String receiveUrl, String authorizationValue) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(URI.create(receiveUrl))
            .header("Accept", "application/json")
            .GET();

        if (authorizationValue != null && !authorizationValue.isBlank()) {
            builder.header("Authorization", authorizationValue);
        }

        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException(
                    "Local receive request failed with status " + response.statusCode() + ": " + summarizeBody(response.body())
                );
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode array = root.isArray() ? root : root.path("data");
            if (!array.isArray()) {
                LOGGER.warn("Unexpected receive payload. Expected array or {data:[...]}. Body: {}", summarizeBody(response.body()));
                return List.of();
            }

            List<InboundMessage> parsed = new ArrayList<>();
            for (JsonNode item : array) {
                String from = text(item, "from");
                String text = text(item, "message");
                Instant receivedAt = parseInstant(item.path("date"));
                parsed.add(new InboundMessage(from, text, receivedAt));
            }

            parsed.sort(Comparator.comparing(InboundMessage::getReceivedAt));
            return parsed;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Local receive polling interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse local receive payload", e);
        }
    }

    private Instant parseInstant(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return Instant.now();
        }
        if (node.isNumber()) {
            long raw = node.asLong();
            return raw > 999_999_999_9L ? Instant.ofEpochMilli(raw) : Instant.ofEpochSecond(raw);
        }
        String text = node.asText();
        if (text == null || text.isBlank()) {
            return Instant.now();
        }

        try {
            long raw = Long.parseLong(text.trim());
            return raw > 999_999_999_9L ? Instant.ofEpochMilli(raw) : Instant.ofEpochSecond(raw);
        } catch (NumberFormatException ignored) {
        }

        try {
            return Instant.parse(text.trim());
        } catch (Exception ignored) {
            return Instant.now();
        }
    }

    private String text(JsonNode item, String key) {
        JsonNode node = item.path(key);
        if (node.isMissingNode() || node.isNull()) {
            return "";
        }
        return node.asText("");
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
}
