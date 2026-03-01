package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ProgramCreateRequest {

    @NotBlank(message = "Program name is required")
    private String name;

    @NotBlank(message = "Program type is required")
    private String type; // UG | PG | DIPLOMA

    @NotNull(message = "Duration is required")
    private Short duration;

    @NotNull(message = "College ID is required")
    private UUID collegeId;
}

