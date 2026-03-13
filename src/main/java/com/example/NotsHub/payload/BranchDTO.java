package com.example.NotsHub.payload;

import com.example.NotsHub.util.SlugUtil;
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
    private String description;
    private UUID programId;
    private Long semestersCountTotal;
    private List<SemesterDTO> semesters;

    public String getSlug() {
        if (slug != null && !slug.isBlank()) {
            return slug;
        }
        return SlugUtil.generateSlug(name);
    }
}

