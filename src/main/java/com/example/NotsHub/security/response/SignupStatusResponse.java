package com.example.NotsHub.security.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupStatusResponse {
    private String status; // "AVAILABLE", "PENDING_VERIFICATION", "ALREADY_REGISTERED"
    private Long otpExpiresInSeconds;
    private Long resendCooldownSeconds;
}
