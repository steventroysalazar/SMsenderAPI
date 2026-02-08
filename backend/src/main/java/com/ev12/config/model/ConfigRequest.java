package com.ev12.config.model;

import jakarta.validation.constraints.NotBlank;

public class ConfigRequest {
    @NotBlank(message = "Device number is required")
    private String deviceNumber;
    private String patientPhone;
    private String alertPhone;
    private Integer heartbeatInterval;
    private String apn;
    private String serverUrl;

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getAlertPhone() {
        return alertPhone;
    }

    public void setAlertPhone(String alertPhone) {
        this.alertPhone = alertPhone;
    }

    public Integer getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Integer heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}
