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
        addIfPresent(commands, "A1,1,1,%s", request.getContactNumber());
        addIfPresent(commands, "P%s", request.getSmsPassword());
        addIfTrue(commands, "loc", request.getRequestLocation());
        addIfBoolean(commands, "Wifi%s", request.getWifiEnabled());
        addIfPresent(commands, "Micvolume%s", request.getMicVolume());
        addIfPresent(commands, "Speakervolume%s", request.getSpeakerVolume());
        addIfPresent(commands, "prefix%s,%s", request.getPrefixEnabled(), request.getPrefixName());
        addIfTrue(commands, "battery", request.getCheckBattery());
        addIfPresent(
            commands,
            "fl%s,%s,%s",
            request.getFallDownEnabled(),
            request.getFallDownSensitivity(),
            toFlag(request.getFallDownCall())
        );
        addIfPresent(
            commands,
            "nmo%s,%s,%s",
            request.getNoMotionEnabled(),
            request.getNoMotionTime(),
            toFlag(request.getNoMotionCall())
        );
        addIfPresent(commands, "S%s,%s", request.getApnEnabled(), request.getApn());
        addIfPresent(commands, "IP%s,%s,%s", request.getServerEnabled(), request.getServerHost(), request.getServerPort());
        addIfBoolean(commands, "S%s", request.getGprsEnabled() == null ? null : (request.getGprsEnabled() ? 2 : 0));
        addWorkingMode(commands, request);
        addIfPresent(commands, "CL%s,%s", request.getContinuousLocateInterval(), request.getContinuousLocateDuration());
        addIfTrue(commands, "status", request.getCheckStatus());

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

    private void addIfPresent(List<String> commands, String template, Integer value) {
        Optional.ofNullable(value)
            .ifPresent(val -> commands.add(String.format(template, val)));
    }

    private void addIfPresent(List<String> commands, String template, Boolean toggle, Object value) {
        if (toggle == null || value == null) {
            return;
        }
        commands.add(String.format(template, toggle ? 1 : 0, value));
    }

    private void addIfPresent(List<String> commands, String template, Boolean toggle, Object valueOne, Object valueTwo) {
        if (toggle == null || valueOne == null || valueTwo == null) {
            return;
        }
        commands.add(String.format(template, toggle ? 1 : 0, valueOne, valueTwo));
    }

    private void addIfPresent(List<String> commands, String template, Object valueOne, Object valueTwo) {
        if (valueOne == null || valueTwo == null) {
            return;
        }
        commands.add(String.format(template, valueOne, valueTwo));
    }

    private void addIfPresent(List<String> commands, String template, Object valueOne, Object valueTwo, Object valueThree) {
        if (valueOne == null || valueTwo == null || valueThree == null) {
            return;
        }
        commands.add(String.format(template, valueOne, valueTwo, valueThree));
    }

    private void addIfTrue(List<String> commands, String value, Boolean toggle) {
        if (Boolean.TRUE.equals(toggle)) {
            commands.add(value);
        }
    }

    private void addIfBoolean(List<String> commands, String template, Boolean toggle) {
        if (toggle == null) {
            return;
        }
        commands.add(String.format(template, toggle ? 1 : 0));
    }

    private void addIfBoolean(List<String> commands, String template, Integer toggle) {
        if (toggle == null) {
            return;
        }
        commands.add(String.format(template, toggle));
    }

    private void addWorkingMode(List<String> commands, ConfigRequest request) {
        String mode = request.getWorkingMode();
        if (mode == null || mode.isBlank()) {
            return;
        }

        String normalized = mode.trim().toLowerCase();
        switch (normalized) {
            case "mode1" -> commands.add("mode1");
            case "mode2" -> addIfPresent(commands, "mode2,%s,%s", request.getWorkingModeInterval(), request.getWorkingModeNoMotionInterval());
            case "mode3" -> addIfPresent(commands, "mode3,%s", request.getWorkingModeInterval());
            case "mode4" -> addIfPresent(commands, "mode4,%s", request.getWorkingModeInterval());
            case "mode5" -> addIfPresent(commands, "mode5,%s", request.getWorkingModeInterval());
            case "mode6" -> addIfPresent(commands, "mode6,%s,%s", request.getWorkingModeInterval(), request.getWorkingModeNoMotionInterval());
            default -> {
            }
        }
    }

    private Integer toFlag(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? 1 : 0;
    }
}
