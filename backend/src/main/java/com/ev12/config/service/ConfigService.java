package com.ev12.config.service;

import com.ev12.config.model.ConfigRequest;
import com.ev12.config.model.ConfigResponse;
import com.ev12.config.model.SmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);
    private final SmsCommandBuilder commandBuilder = new SmsCommandBuilder();

    public ConfigResponse sendConfiguration(ConfigRequest request) {
        List<SmsMessage> messages = commandBuilder.build(request);
        if (!messages.isEmpty()) {
            String payloadPreview = messages.stream()
                .map(SmsMessage::getBody)
                .collect(Collectors.joining(" | "));
            LOGGER.info("Prepared SMS payload for {}: {}", request.getDeviceNumber(), payloadPreview);
        }
        String baseUrl = System.getenv().getOrDefault("PHILSMS_BASE_URL", "https://dashboard.philsms.com/api/v3");
        String messagesPath = System.getenv().getOrDefault("PHILSMS_MESSAGES_PATH", "/messages");
        String sendUrl = System.getenv("PHILSMS_SEND_URL");
        String contentType = System.getenv().getOrDefault("PHILSMS_CONTENT_TYPE", "application/json");
        boolean includeTokenInBody = Boolean.parseBoolean(System.getenv().getOrDefault("PHILSMS_TOKEN_IN_BODY", "false"));
        String apiToken = System.getenv("PHILSMS_API_TOKEN");
        String senderId = System.getenv("PHILSMS_SENDER_ID");
        boolean dryRun = Boolean.parseBoolean(System.getenv().getOrDefault("SMS_DRY_RUN", "false"));

        if (!dryRun && (isBlank(apiToken) || isBlank(senderId))) {
            throw new ResponseStatusException(
                BAD_REQUEST,
                "PhilSMS credentials are missing. Set PHILSMS_API_TOKEN and PHILSMS_SENDER_ID or enable SMS_DRY_RUN."
            );
        }

        PhilSmsSender sender = new PhilSmsSender(
            baseUrl,
            apiToken,
            senderId,
            messagesPath,
            sendUrl,
            contentType,
            includeTokenInBody,
            dryRun
        );
        try {
            sender.send(messages);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(
                BAD_GATEWAY,
                "SMS provider request failed. Verify PHILSMS_BASE_URL/PHILSMS_MESSAGES_PATH and credentials. " + ex.getMessage(),
                ex
            );
        }
        return new ConfigResponse(request.getDeviceNumber(), messages);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
