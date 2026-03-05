package com.example.NotsHub.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResendEmailOtpRequest {

    @NotBlank
    @Email
    @Size(max = 50)
    private String email;
}
