package com.example.NotsHub.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class NotesCreateRequest {

    @NotBlank(message = "Notes title is required")
    private String title;

    private String description;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotBlank(message = "File key is required")
    private String fileKey;

    @NotBlank(message = "File type is required")
    private String fileType; // PDF | DOCX | PPTX | IMAGE

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotNull(message = "Uploader ID is required")
    private UUID uploadedById;
}

