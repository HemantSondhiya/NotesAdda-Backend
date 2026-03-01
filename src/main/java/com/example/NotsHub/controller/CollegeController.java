package com.example.NotsHub.controller;

import com.example.NotsHub.payload.APIResponse;
import com.example.NotsHub.payload.CollegeCreateRequest;
import com.example.NotsHub.payload.CollegeDTO;
import com.example.NotsHub.service.CollegeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/colleges")
public class CollegeController {
    @Autowired
    private CollegeService collegeService;

    @PostMapping
   // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createCollege(
            @Valid @RequestBody CollegeCreateRequest request) {
        CollegeDTO collegeDTO = collegeService.createCollege(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new APIResponse("College created successfully", true, collegeDTO));
    }

    // GET /api/colleges
    @GetMapping
    public ResponseEntity<?> getAllColleges() {
        List<CollegeDTO> colleges = collegeService.getAllColleges();
        return ResponseEntity.status(HttpStatus.OK).body(
                new APIResponse("Colleges", true, colleges)
        );
    }
}
