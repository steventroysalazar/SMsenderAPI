package com.ev12.config.service;

import com.ev12.config.model.SmsMessage;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TwilioSmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwilioSmsSender.class);

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final boolean dryRun;

    public TwilioSmsSender(String accountSid, String authToken, String fromNumber, boolean dryRun) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.dryRun = dryRun;
    }

    public void send(List<SmsMessage> messages) {
        if (dryRun) {
            messages.forEach(message -> LOGGER.info("[DRY RUN] SMS to {}: {}", message.getTo(), message.getBody()));
            return;
        }

        if (accountSid == null || accountSid.isBlank()
            || authToken == null || authToken.isBlank()
            || fromNumber == null || fromNumber.isBlank()) {
            throw new IllegalStateException("Twilio credentials are missing. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_FROM_NUMBER.");
        }

        Twilio.init(accountSid, authToken);
        for (SmsMessage message : messages) {
            Message.creator(new PhoneNumber(message.getTo()), new PhoneNumber(fromNumber), message.getBody())
                .create();
        }
    }
}
