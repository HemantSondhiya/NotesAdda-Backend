package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.payload.UniversityCreateRequest;
import com.example.NotsHub.payload.UniversityDTO;
import com.example.NotsHub.payload.ProgramDTO;
import com.example.NotsHub.service.ProgramService;
import com.example.NotsHub.service.UniversityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/universities")
@Tag(name = "Universities", description = "Endpoints for managing universities")
public class UniversityController {
    @Autowired
    private UniversityService universityService;
    @Autowired
    private ProgramService programService;

    @PostMapping
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> createUniversity(
            @Valid @RequestBody UniversityCreateRequest request) {
        UniversityDTO universityDTO = universityService.createUniversity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new APIResponse("University created successfully", true, universityDTO));
    }

    @GetMapping
    public ResponseEntity<?> getAllUniversities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UniversityDTO> universities = universityService.getAllUniversities(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(
                new APIResponse("Universities", true, PagedResponse.from(universities))
        );
    }

    @Operation(summary = "Get a university by its slug")
    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getUniversityBySlug(@PathVariable String slug) {
        UniversityDTO university = universityService.getBySlug(slug);
        return ResponseEntity.ok(new APIResponse("University retrieved successfully", true, university));
    }

    @GetMapping("/{id}/programs")
    public ResponseEntity<?> getProgramsByUniversity(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProgramDTO> programs = programService.getProgramsByUniversity(id, page, size);
        return ResponseEntity.ok(
                new APIResponse("Programs retrieved successfully for university", true, PagedResponse.from(programs))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateUniversity(
            @PathVariable UUID id,
            @Valid @RequestBody UniversityCreateRequest request) {
        UniversityDTO updated = universityService.updateUniversity(id, request);
        return ResponseEntity.ok(new APIResponse("University updated successfully", true, updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteUniversity(@PathVariable UUID id) {
        universityService.deleteUniversity(id);
        return ResponseEntity.ok(new APIResponse("University deleted successfully", true, null));
    }
}
