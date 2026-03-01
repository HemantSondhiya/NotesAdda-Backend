package com.example.NotsHub.payload;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeDTO {
    private UUID id;
    private String name;
    private String code;
    private String city;
    private String state;
    private String logoUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
