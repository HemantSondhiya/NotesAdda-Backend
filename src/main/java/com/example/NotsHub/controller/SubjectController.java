package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.NotesDTO;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.payload.SubjectCreateRequest;
import com.example.NotsHub.payload.SubjectDTO;
import com.example.NotsHub.service.NotesService;
import com.example.NotsHub.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Endpoints for managing subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private NotesService notesService;

    @PostMapping
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectCreateRequest request) {
        SubjectDTO subjectDTO = subjectService.createSubject(request);
        return ResponseEntity.ok(new APIResponse<>("Subject created successfully", true, subjectDTO));
    }

    @GetMapping
    public ResponseEntity<?> getSubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Page<SubjectDTO> subjects = subjectService.getSubjects(page, size);
        return ResponseEntity.ok(new APIResponse<>("Subjects retrieved successfully", true, PagedResponse.from(subjects)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSubjectById(@PathVariable UUID id) {
        SubjectDTO subject = subjectService.getSubjectById(id);
        return ResponseEntity.ok(new APIResponse<>("Subject retrieved successfully", true, subject));
    }

    @Operation(summary = "Get a subject by its slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getSubjectBySlug(@PathVariable String slug) {
        SubjectDTO subject = subjectService.getBySlug(slug);
        return ResponseEntity.ok(new APIResponse<>("Subject retrieved successfully", true, subject));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<?> getNotesBySubject(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NotesDTO> notes = notesService.getNotesBySubject(id, page, size);
        return ResponseEntity.ok(new APIResponse<>("Notes retrieved successfully for subject", true, PagedResponse.from(notes)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteSubject(@PathVariable UUID id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.ok(new APIResponse<>("Subject deleted successfully", true, null));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateSubject(@PathVariable UUID id, @Valid @RequestBody SubjectCreateRequest request) {
        SubjectDTO subject = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(new APIResponse<>("Subject updated successfully", true, subject));
    }
}
