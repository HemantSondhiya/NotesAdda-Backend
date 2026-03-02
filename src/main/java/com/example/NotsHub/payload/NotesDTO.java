package com.example.NotsHub.payload;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotesDTO {
    private UUID id;
    private String title;
    private String description;
    private String fileUrl;
    private String fileKey;
    private String fileType; // PDF | DOCX | PPTX | IMAGE
    private Boolean isApproved;
    private String rejectionNote;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private UUID subjectId;
    private Long uploadedById;
    private Long approvedById;
}

