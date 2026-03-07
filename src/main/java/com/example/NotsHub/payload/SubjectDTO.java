package com.example.NotsHub.payload;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {
    private UUID id;
    private String name;
    private String slug;
    private String code;
    private Short credits;
    private String syllabusUrl;
    private UUID semesterId;
    private List<NotesDTO> notes;
}

