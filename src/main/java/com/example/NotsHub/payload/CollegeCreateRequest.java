package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CollegeCreateRequest {

    @NotBlank(message = "College name is required")
    private String name;

    @NotBlank(message = "College code is required")
    @Size(max = 20, message = "Code must be under 20 characters")
    private String code;

    private String city;
    private String state;
    private String logoUrl;
}
