package com.example.NotsHub.payload;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private UUID id;
    private String name;
    private String slug;
    private String code;
    private UUID programId;
    private List<SemesterDTO> semesters;
}

