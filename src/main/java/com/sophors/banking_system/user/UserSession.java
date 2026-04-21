package com.sophors.banking_system.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String deviceType;
    private String deviceName;
    private String deviceVersion;
    private String osName;
    private String osVersion;
    private String browserName;
    private String browserVersion;
    private String userAgent;
    private String ipAddress;
    private Double lat;
    private Double lng;
    private LocalDateTime loginAt;
}
