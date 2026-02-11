package com.ev12.config.service;

import com.ev12.config.model.SmsMessage;

import java.util.List;

/**
 * @deprecated Replaced by {@link KudositySmsSender}. This class delegates for backward compatibility.
 */
@Deprecated
public class PhilSmsSender {
    private final KudositySmsSender delegate;

    public PhilSmsSender(String sendUrl, String apiKey, String senderId, boolean dryRun) {
        this.delegate = new KudositySmsSender(sendUrl, apiKey, senderId, dryRun);
    }

    public void send(List<SmsMessage> messages) {
        delegate.send(messages);
    }
}
