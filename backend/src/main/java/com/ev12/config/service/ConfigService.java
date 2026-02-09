package com.ev12.config.service;

import com.ev12.config.model.ConfigRequest;
import com.ev12.config.model.ConfigResponse;
import com.ev12.config.model.SmsMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class ConfigService {
    private final SmsCommandBuilder commandBuilder = new SmsCommandBuilder();

    public ConfigResponse sendConfiguration(ConfigRequest request) {
        List<SmsMessage> messages = commandBuilder.build(request);
        String apiKey = System.getenv("VONAGE_API_KEY");
        String apiSecret = System.getenv("VONAGE_API_SECRET");
        String fromNumber = System.getenv("VONAGE_FROM_NUMBER");
        boolean dryRun = Boolean.parseBoolean(System.getenv().getOrDefault("SMS_DRY_RUN", "false"));

        if (!dryRun && (isBlank(apiKey) || isBlank(apiSecret) || isBlank(fromNumber))) {
            throw new ResponseStatusException(
                BAD_REQUEST,
                "Vonage credentials are missing. Set VONAGE_API_KEY, VONAGE_API_SECRET, and VONAGE_FROM_NUMBER or enable SMS_DRY_RUN."
            );
        }

        VonageSmsSender sender = new VonageSmsSender(apiKey, apiSecret, fromNumber, dryRun);
        sender.send(messages);
        return new ConfigResponse(request.getDeviceNumber(), messages);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
