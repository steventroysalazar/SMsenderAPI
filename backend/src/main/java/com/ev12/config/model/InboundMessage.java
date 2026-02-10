package com.ev12.config.model;

import java.time.Instant;

public class InboundMessage {
    private final String from;
    private final String text;
    private final Instant receivedAt;

    public InboundMessage(String from, String text, Instant receivedAt) {
        this.from = from;
        this.text = text;
        this.receivedAt = receivedAt;
    }

    public String getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
