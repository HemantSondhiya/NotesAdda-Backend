package com.example.NotsHub.security.request;

import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "Username can only contain letters, numbers, ., _, - and no spaces"
    )
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    private Set<String> role;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

}