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
    private UUID branchId;
    private List<SubjectDTO> subjects;
}

