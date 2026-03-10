package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class BranchCreateRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    @NotBlank(message = "Branch code is required")
    @Size(max = 20, message = "Code must be under 20 characters")
    private String code;

    @Size(max = 1000, message = "Description is too long")
    private String description;

    @NotNull(message = "Program ID is required")
    private UUID programId;
}

