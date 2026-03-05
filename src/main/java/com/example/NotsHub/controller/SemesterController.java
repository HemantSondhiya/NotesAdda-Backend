package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.payload.SemesterCreateRequest;
import com.example.NotsHub.payload.SemesterDTO;
import com.example.NotsHub.payload.SubjectDTO;
import com.example.NotsHub.service.SemesterService;
import com.example.NotsHub.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/semesters")
public class SemesterController {

    @Autowired
    private SemesterService semesterService;
    @Autowired
    private SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> createSemester(@Valid @RequestBody SemesterCreateRequest request) {
        SemesterDTO semesterDTO = semesterService.createSemester(request);
        return ResponseEntity.ok(new APIResponse<>("Semester created successfully", true, semesterDTO));
    }
    @GetMapping
    public ResponseEntity<?> getAllSemesters(@RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        Page<SemesterDTO> semester = semesterService.getAllSemester(page, size);

        return ResponseEntity.ok(new APIResponse<>("Semesters retrieved successfully", true, PagedResponse.from(semester)));
    }

    @GetMapping("/{id}/subjects")
    public ResponseEntity<?> getSubjectsBySemester(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SubjectDTO> subjects = subjectService.getSubjectsBySemester(id, page, size);
        Page<SubjectSummaryItem> summary = subjects.map(s -> new SubjectSummaryItem(
                s.getId(),
                s.getName(),
                s.getCode()
        ));
        return ResponseEntity.ok(new APIResponse<>("Subjects retrieved successfully for semester", true, PagedResponse.from(summary)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteSemester(@PathVariable UUID id) {
        semesterService.deleteSemester(id);
        return ResponseEntity.ok(new APIResponse<>("Semester deleted successfully", true, null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateSemester(@PathVariable UUID id, @Valid @RequestBody SemesterCreateRequest request) {
        SemesterDTO updated = semesterService.updateSemester(id, request);
        return ResponseEntity.ok(new APIResponse<>("Semester updated successfully", true, updated));
    }

    private record SubjectSummaryItem(UUID id, String name, String code) {
    }
}
