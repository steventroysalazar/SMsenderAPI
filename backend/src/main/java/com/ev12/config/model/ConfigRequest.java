package com.ev12.config.model;

import jakarta.validation.constraints.NotBlank;

public class ConfigRequest {
    @NotBlank(message = "Device number is required")
    private String deviceNumber;
    private String contactNumber;
    private String smsPassword;
    private Boolean requestLocation;
    private Boolean wifiEnabled;
    private Integer micVolume;
    private Integer speakerVolume;
    private Boolean prefixEnabled;
    private String prefixName;
    private Boolean checkBattery;
    private Boolean fallDownEnabled;
    private Integer fallDownSensitivity;
    private Boolean fallDownCall;
    private Boolean noMotionEnabled;
    private String noMotionTime;
    private Boolean noMotionCall;
    private Boolean apnEnabled;
    private String apn;
    private Boolean serverEnabled;
    private String serverHost;
    private Integer serverPort;
    private Boolean gprsEnabled;
    private String workingMode;
    private String workingModeInterval;
    private String workingModeNoMotionInterval;
    private String continuousLocateInterval;
    private String continuousLocateDuration;
    private Boolean checkStatus;

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getSmsPassword() {
        return smsPassword;
    }

    public void setSmsPassword(String smsPassword) {
        this.smsPassword = smsPassword;
    }

    public Boolean getRequestLocation() {
        return requestLocation;
    }

    public void setRequestLocation(Boolean requestLocation) {
        this.requestLocation = requestLocation;
    }

    public Boolean getWifiEnabled() {
        return wifiEnabled;
    }

    public void setWifiEnabled(Boolean wifiEnabled) {
        this.wifiEnabled = wifiEnabled;
    }

    public Integer getMicVolume() {
        return micVolume;
    }

    public void setMicVolume(Integer micVolume) {
        this.micVolume = micVolume;
    }

    public Integer getSpeakerVolume() {
        return speakerVolume;
    }

    public void setSpeakerVolume(Integer speakerVolume) {
        this.speakerVolume = speakerVolume;
    }

    public Boolean getPrefixEnabled() {
        return prefixEnabled;
    }

    public void setPrefixEnabled(Boolean prefixEnabled) {
        this.prefixEnabled = prefixEnabled;
    }

    public String getPrefixName() {
        return prefixName;
    }

    public void setPrefixName(String prefixName) {
        this.prefixName = prefixName;
    }

    public Boolean getCheckBattery() {
        return checkBattery;
    }

    public void setCheckBattery(Boolean checkBattery) {
        this.checkBattery = checkBattery;
    }

    public Boolean getFallDownEnabled() {
        return fallDownEnabled;
    }

    public void setFallDownEnabled(Boolean fallDownEnabled) {
        this.fallDownEnabled = fallDownEnabled;
    }

    public Integer getFallDownSensitivity() {
        return fallDownSensitivity;
    }

    public void setFallDownSensitivity(Integer fallDownSensitivity) {
        this.fallDownSensitivity = fallDownSensitivity;
    }

    public Boolean getFallDownCall() {
        return fallDownCall;
    }

    public void setFallDownCall(Boolean fallDownCall) {
        this.fallDownCall = fallDownCall;
    }

    public Boolean getNoMotionEnabled() {
        return noMotionEnabled;
    }

    public void setNoMotionEnabled(Boolean noMotionEnabled) {
        this.noMotionEnabled = noMotionEnabled;
    }

    public String getNoMotionTime() {
        return noMotionTime;
    }

    public void setNoMotionTime(String noMotionTime) {
        this.noMotionTime = noMotionTime;
    }

    public Boolean getNoMotionCall() {
        return noMotionCall;
    }

    public void setNoMotionCall(Boolean noMotionCall) {
        this.noMotionCall = noMotionCall;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public Boolean getApnEnabled() {
        return apnEnabled;
    }

    public void setApnEnabled(Boolean apnEnabled) {
        this.apnEnabled = apnEnabled;
    }

    public Boolean getServerEnabled() {
        return serverEnabled;
    }

    public void setServerEnabled(Boolean serverEnabled) {
        this.serverEnabled = serverEnabled;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Boolean getGprsEnabled() {
        return gprsEnabled;
    }

    public void setGprsEnabled(Boolean gprsEnabled) {
        this.gprsEnabled = gprsEnabled;
    }

    public String getWorkingMode() {
        return workingMode;
    }

    public void setWorkingMode(String workingMode) {
        this.workingMode = workingMode;
    }

    public String getWorkingModeInterval() {
        return workingModeInterval;
    }

    public void setWorkingModeInterval(String workingModeInterval) {
        this.workingModeInterval = workingModeInterval;
    }

    public String getWorkingModeNoMotionInterval() {
        return workingModeNoMotionInterval;
    }

    public void setWorkingModeNoMotionInterval(String workingModeNoMotionInterval) {
        this.workingModeNoMotionInterval = workingModeNoMotionInterval;
    }

    public String getContinuousLocateInterval() {
        return continuousLocateInterval;
    }

    public void setContinuousLocateInterval(String continuousLocateInterval) {
        this.continuousLocateInterval = continuousLocateInterval;
    }

    public String getContinuousLocateDuration() {
        return continuousLocateDuration;
    }

    public void setContinuousLocateDuration(String continuousLocateDuration) {
        this.continuousLocateDuration = continuousLocateDuration;
    }

    public Boolean getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(Boolean checkStatus) {
        this.checkStatus = checkStatus;
    }
}
