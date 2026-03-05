package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.BranchCreateRequest;
import com.example.NotsHub.payload.BranchDTO;
import com.example.NotsHub.payload.PagedResponse;
import com.example.NotsHub.payload.SemesterDTO;
import com.example.NotsHub.service.BranchService;
import com.example.NotsHub.service.SemesterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    @Autowired
    private BranchService branchService;
    @Autowired
    private SemesterService semesterService;
    @PostMapping
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> createBranch(@Valid @RequestBody BranchCreateRequest request) {
        BranchDTO branchDTO = branchService.createBranch(request);
        return ResponseEntity.ok(
                new APIResponse("Branch created successfully", true, branchDTO)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBranchById(@PathVariable UUID id) {
        BranchDTO branchDTO = branchService.getBranchById(id);
        return ResponseEntity.ok(
                new APIResponse("Branch retrieved successfully", true, branchDTO)
        );
    }

    @GetMapping("/{id}/semesters")
    public ResponseEntity<?> getSemestersByBranch(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SemesterDTO> semesters = semesterService.getSemestersByBranch(id, page, size);
        Page<SemesterSummaryItem> summary = semesters.map(s -> new SemesterSummaryItem(
                s.getId(),
                s.getNumber(),
                s.getSemester()
        ));
        return ResponseEntity.ok(
                new APIResponse("Semesters retrieved successfully for branch", true, PagedResponse.from(summary))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateBranch(@PathVariable UUID id, @Valid @RequestBody BranchCreateRequest request) {
        BranchDTO branchDTO = branchService.updateBranch(id, request);
        return ResponseEntity.ok(
                new APIResponse("Branch updated successfully", true, branchDTO)
        );
    }
    @GetMapping
    public ResponseEntity<?> getAllBranches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BranchDTO> branches = branchService.getAllBranches(page, size);
        return ResponseEntity.ok(
                new APIResponse("Branches retrieved successfully", true, PagedResponse.from(branches))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteBranch(@PathVariable UUID id) {
        branchService.deleteBranch(id);
        return ResponseEntity.ok(
                new APIResponse("Branch deleted successfully", true, null)
        );
    }

    private record SemesterSummaryItem(UUID id, Short number, String semester) {
    }
}
