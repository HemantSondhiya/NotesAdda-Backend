package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class NotesCreateRequest {

    @NotBlank(message = "Notes title is required")
    private String title;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @NotBlank(message = "File URL is required")
    @Pattern(regexp = "^(https?://).+", message = "File URL must be http/https")
    @Size(max = 255, message = "File URL is too long")
    private String fileUrl;

    @NotBlank(message = "File key is required")
    @Size(max = 255, message = "File key is too long")
    private String fileKey;

    @NotBlank(message = "File type is required")
    @Pattern(regexp = "^(?i)PDF$", message = "Only PDF file type is allowed")
    private String fileType; // PDF only

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;
}

