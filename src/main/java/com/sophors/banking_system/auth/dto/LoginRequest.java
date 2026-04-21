package com.sophors.banking_system.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private Double lat;
    private Double lng;
}
