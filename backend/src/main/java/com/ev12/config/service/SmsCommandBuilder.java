package com.ev12.config.service;

import com.ev12.config.model.ConfigRequest;
import com.ev12.config.model.SmsMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SmsCommandBuilder {
    public List<SmsMessage> build(ConfigRequest request) {
        String commandPayload = buildCommandPayload(request);
        return splitPayload(request.getDeviceNumber(), commandPayload, 150);
    }

    private String buildCommandPayload(ConfigRequest request) {
        List<String> commands = new ArrayList<>();
        addIfPresent(commands, "PTPHONE:%s", request.getPatientPhone());
        addIfPresent(commands, "ALERT:%s", request.getAlertPhone());
        if (request.getHeartbeatInterval() != null) {
            commands.add("HEART:" + request.getHeartbeatInterval());
        }
        addIfPresent(commands, "APN:%s", request.getApn());
        addIfPresent(commands, "SERVER:%s", request.getServerUrl());

        return String.join(";", commands);
    }

    private List<SmsMessage> splitPayload(String deviceNumber, String payload, int maxLength) {
        List<SmsMessage> messages = new ArrayList<>();
        if (payload.isBlank()) {
            return messages;
        }

        if (payload.length() <= maxLength) {
            messages.add(new SmsMessage(deviceNumber, payload));
            return messages;
        }

        String firstChunk = payload.substring(0, maxLength);
        String remainder = payload.substring(maxLength);
        messages.add(new SmsMessage(deviceNumber, firstChunk));
        messages.add(new SmsMessage(deviceNumber, remainder));

        return messages;
    }

    private void addIfPresent(List<String> commands, String template, String value) {
        Optional.ofNullable(value)
            .map(String::trim)
            .filter(val -> !val.isEmpty())
            .ifPresent(val -> commands.add(String.format(template, val)));
    }
}
