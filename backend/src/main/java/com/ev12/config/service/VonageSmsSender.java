package com.ev12.config.service;

import com.ev12.config.model.SmsMessage;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VonageSmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(VonageSmsSender.class);

    private final String apiKey;
    private final String apiSecret;
    private final String fromNumber;
    private final boolean dryRun;

    public VonageSmsSender(String apiKey, String apiSecret, String fromNumber, boolean dryRun) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.fromNumber = fromNumber;
        this.dryRun = dryRun;
    }

    public void send(List<SmsMessage> messages) {
        if (dryRun) {
            messages.forEach(message -> LOGGER.info("[DRY RUN] SMS to {}: {}", message.getTo(), message.getBody()));
            return;
        }

        if (apiKey == null || apiKey.isBlank()
            || apiSecret == null || apiSecret.isBlank()
            || fromNumber == null || fromNumber.isBlank()) {
            throw new IllegalStateException("Vonage credentials are missing. Set VONAGE_API_KEY, VONAGE_API_SECRET, and VONAGE_FROM_NUMBER.");
        }

        VonageClient client = VonageClient.builder()
            .apiKey(apiKey)
            .apiSecret(apiSecret)
            .build();

        for (SmsMessage message : messages) {
            TextMessage textMessage = new TextMessage(fromNumber, message.getTo(), message.getBody());
            SmsSubmissionResponse response = client.getSmsClient().submitMessage(textMessage);
            if (response.getMessages().stream().anyMatch(resp -> resp.getStatus() != MessageStatus.OK)) {
                LOGGER.warn("Vonage SMS failed for {}: {}", message.getTo(), response.getMessages());
            }
        }
    }
}
