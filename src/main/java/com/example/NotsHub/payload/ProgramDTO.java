package com.example.NotsHub.payload;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDTO {
    private UUID id;
    private String name;
    private String type; // UG | PG | DIPLOMA
    private Short duration;
    private UUID collegeId;
    private List<BranchDTO> branches;
}

