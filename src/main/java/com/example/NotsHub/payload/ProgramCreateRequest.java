package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ProgramCreateRequest {

    @NotBlank(message = "Program name is required")
    private String name;

    private String description;

    @NotBlank(message = "Program type is required")
    private String type;

    @NotNull(message = "Duration is required")
    private Short duration;

    @NotNull(message = "University ID is required")
    private UUID universityId;
}
