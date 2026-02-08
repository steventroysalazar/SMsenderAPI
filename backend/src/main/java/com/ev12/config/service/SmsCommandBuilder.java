package com.ev12.config.service;

import com.ev12.config.model.ConfigRequest;
import com.ev12.config.model.SmsMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SmsCommandBuilder {
    public List<SmsMessage> build(ConfigRequest request) {
        List<SmsMessage> messages = new ArrayList<>();
        String deviceNumber = request.getDeviceNumber();

        addIfPresent(messages, deviceNumber, "PTPHONE:%s", request.getPatientPhone());
        addIfPresent(messages, deviceNumber, "ALERT:%s", request.getAlertPhone());
        if (request.getHeartbeatInterval() != null) {
            messages.add(new SmsMessage(deviceNumber, "HEART:" + request.getHeartbeatInterval()));
        }
        addIfPresent(messages, deviceNumber, "APN:%s", request.getApn());
        addIfPresent(messages, deviceNumber, "SERVER:%s", request.getServerUrl());

        return messages;
    }

    private void addIfPresent(List<SmsMessage> messages, String deviceNumber, String template, String value) {
        Optional.ofNullable(value)
            .map(String::trim)
            .filter(val -> !val.isEmpty())
            .ifPresent(val -> messages.add(new SmsMessage(deviceNumber, String.format(template, val))));
    }
}
