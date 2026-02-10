package com.ev12.config.service;

import com.ev12.config.model.InboundMessage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class InboundMessageStore {
    private final List<InboundMessage> messages = Collections.synchronizedList(new ArrayList<>());

    public void addMessage(String from, String text) {
        messages.add(new InboundMessage(from, text, Instant.now()));
    }

    public List<InboundMessage> getMessages() {
        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }
}
