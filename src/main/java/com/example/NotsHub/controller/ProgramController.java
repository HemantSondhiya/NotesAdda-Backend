package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.payload.ProgramCreateRequest;
import com.example.NotsHub.payload.ProgramDTO;
import com.example.NotsHub.payload.BranchDTO;
import com.example.NotsHub.service.BranchService;
import com.example.NotsHub.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    @Autowired
    private ProgramService programService;
    @Autowired
    private BranchService branchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> createProgram(
            @Valid @RequestBody ProgramCreateRequest request) {
        ProgramDTO programDTO = programService.createProgram(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new APIResponse("Program created successfully", true, programDTO));
    }

    @GetMapping
    public ResponseEntity<?> getAllPrograms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProgramDTO> programs = programService.getAllPrograms(page, size);
        return ResponseEntity.ok(
                new APIResponse("All programs retrieved successfully", true, PagedResponse.from(programs)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProgramById(@PathVariable UUID id) {
        ProgramDTO program = programService.getProgramById(id);
        return ResponseEntity.ok(
                new APIResponse("Program retrieved successfully", true, program));
    }

    @GetMapping("/{id}/branches")
    public ResponseEntity<?> getBranchesByProgram(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BranchDTO> branches = branchService.getBranchesByProgram(id, page, size);
        return ResponseEntity.ok(
                new APIResponse("Branches retrieved successfully for program", true, PagedResponse.from(branches))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody ProgramCreateRequest request) {
        ProgramDTO updated = programService.updateProgram(id, request);
        return ResponseEntity.ok(
                new APIResponse("Program updated successfully", true, updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteProgram(@PathVariable UUID id) {
        programService.deleteProgram(id);
        return ResponseEntity.ok(
                new APIResponse("Program deleted successfully", true, null));
    }
}
