package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UniversityCreateRequest {

    @NotBlank(message = "University name is required")
    private String name;

    @NotBlank(message = "University code is required")
    @Size(max = 20, message = "Code must be under 20 characters")
    private String code;

    private String description;

    private String city;
    private String state;
    private String logoUrl;
}
