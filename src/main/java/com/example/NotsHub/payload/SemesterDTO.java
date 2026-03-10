package com.example.NotsHub.payload;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterDTO {
    private UUID id;
    private Short number;
    private String semester;
    private UUID branchId;
    private String branchName;
    private String branchCode;
    private UUID programId;
    private String programName;
    private UUID universityId;
    private String universityName;
    private Long subjectsCountTotal;
    private List<SubjectDTO> subjects;
}

