package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotesUpdateRequest {

    @NotBlank(message = "Notes title is required")
    private String title;

    @Size(max = 5000, message = "Description is too long")
    private String description;
}
