package com.example.NotsHub.payload;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityDTO {
    private UUID id;
    private String name;
    private String slug;
    private String code;
    private String description;
    private String city;
    private String state;
    private String logoUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long programsCountTotal;
    private List<ProgramDTO> programs;
}
