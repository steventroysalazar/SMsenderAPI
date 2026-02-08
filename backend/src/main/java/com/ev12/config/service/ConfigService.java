package com.ev12.config.service;

import com.ev12.config.model.ConfigRequest;
import com.ev12.config.model.ConfigResponse;
import com.ev12.config.model.SmsMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigService {
    private final SmsCommandBuilder commandBuilder = new SmsCommandBuilder();

    public ConfigResponse sendConfiguration(ConfigRequest request) {
        List<SmsMessage> messages = commandBuilder.build(request);
        TwilioSmsSender sender = new TwilioSmsSender(
            System.getenv("TWILIO_ACCOUNT_SID"),
            System.getenv("TWILIO_AUTH_TOKEN"),
            System.getenv("TWILIO_FROM_NUMBER"),
            Boolean.parseBoolean(System.getenv().getOrDefault("SMS_DRY_RUN", "true"))
        );
        sender.send(messages);
        return new ConfigResponse(request.getDeviceNumber(), messages);
    }
}
