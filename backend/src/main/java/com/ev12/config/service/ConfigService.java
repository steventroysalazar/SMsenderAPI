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

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

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

        String sendUrl = System.getenv().getOrDefault("KUDOSITY_SEND_URL", "https://api.kudosity.com/v1/messages");
        String apiKey = System.getenv("KUDOSITY_API_KEY");
        String senderId = System.getenv("KUDOSITY_SENDER_ID");
        boolean dryRun = Boolean.parseBoolean(System.getenv().getOrDefault("SMS_DRY_RUN", "false"));

        if (!dryRun && (isBlank(apiKey) || isBlank(senderId))) {
            throw new ResponseStatusException(
                BAD_REQUEST,
                "Kudosity credentials are missing. Set KUDOSITY_API_KEY and KUDOSITY_SENDER_ID or enable SMS_DRY_RUN."
            );
        }

        KudositySmsSender sender = new KudositySmsSender(sendUrl, apiKey, senderId, dryRun);
        try {
            sender.send(messages);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(
                BAD_GATEWAY,
                "SMS provider request failed. Verify KUDOSITY_SEND_URL and credentials. " + ex.getMessage(),
                ex
            );
        }

        return new ConfigResponse(request.getDeviceNumber(), messages);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
