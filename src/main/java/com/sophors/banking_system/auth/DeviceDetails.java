package com.sophors.banking_system.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeviceDetails {

    private final String deviceType;
    private final String deviceName;
    private final String deviceVersion;
    private final String osName;
    private final String osVersion;
    private final String browserName;
    private final String browserVersion;
}
