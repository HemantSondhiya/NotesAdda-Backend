package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.payload.UniversityCreateRequest;
import com.example.NotsHub.payload.UniversityDTO;
import com.example.NotsHub.service.UniversityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/universities")
public class UniversityController {
    @Autowired
    private UniversityService universityService;

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
