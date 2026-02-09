package com.ev12.config.model;

import java.util.List;

public class ConfigResponse {
    private final String deviceNumber;
    private final List<SmsMessage> messages;

    public ConfigResponse(String deviceNumber, List<SmsMessage> messages) {
        this.deviceNumber = deviceNumber;
        this.messages = messages;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public List<SmsMessage> getMessages() {
        return messages;
    }
}
