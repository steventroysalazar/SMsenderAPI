package com.ev12.config.service;

import com.ev12.config.model.SmsMessage;

import java.util.List;

/**
 * @deprecated Replaced by {@link LocalSmsSender}. This class delegates for backward compatibility.
 */
@Deprecated
public class KudositySmsSender {
    private final LocalSmsSender delegate;

    public KudositySmsSender(String sendUrl, String authorizationValue, String senderId, boolean dryRun) {
        this.delegate = new LocalSmsSender(sendUrl, authorizationValue, dryRun);
    }

    public void send(List<SmsMessage> messages) {
        delegate.send(messages);
    }
}
