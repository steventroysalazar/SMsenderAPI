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
        String apiToken = "1315|xfUzDUQrpRYXoy1oKo7Hl3iTexT6JXMx4Y9p5J2w612c7c81";
        String senderId = "EV12SMS";
        boolean dryRun = Boolean.parseBoolean(System.getenv().getOrDefault("SMS_DRY_RUN", "false"));

        if (!dryRun && (isBlank(apiToken) || isBlank(senderId))) {
            throw new ResponseStatusException(
                BAD_REQUEST,
                "PhilSMS credentials are missing. Set PHILSMS_API_TOKEN and PHILSMS_SENDER_ID or enable SMS_DRY_RUN."
            );
        }

        PhilSmsSender sender = new PhilSmsSender(baseUrl, apiToken, senderId, dryRun);
        sender.send(messages);
        return new ConfigResponse(request.getDeviceNumber(), messages);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
