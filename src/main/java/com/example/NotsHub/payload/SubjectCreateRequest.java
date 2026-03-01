package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SubjectCreateRequest {

    @NotBlank(message = "Subject name is required")
    private String name;

    @NotBlank(message = "Subject code is required")
    private String code;

    @NotNull(message = "Credits is required")
    private Short credits;

    private String syllabusUrl;

    @NotNull(message = "Semester ID is required")
    private UUID semesterId;
}

