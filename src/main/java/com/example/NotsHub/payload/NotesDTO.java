package com.example.NotsHub.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotesDTO {
    private UUID id;
    private String title;
    private String slug;
    private String description;

    @JsonIgnore
    private String fileUrl;

    @JsonIgnore
    private String fileKey;

    private String downloadUrl;
    private String viewUrl;
    private String fileType; // PDF | DOCX | PPTX | IMAGE
    private Boolean isApproved;
    private String rejectionNote;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private UUID subjectId;
    private String subjectName;
    private String subjectSlug;
    private Short semesterNumber;
    private String branchSlug;
    private String programSlug;
    private String universitySlug;
    private UUID uploadedById;
    private String uploaderName;
    private String uploaderEmail;
    private Long uploaderTotalNotes;
    private UUID approvedById;
    private String approvedByEmail;
}

