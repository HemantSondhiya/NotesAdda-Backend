package com.example.NotsHub.service;

import com.example.NotsHub.payload.SemesterCreateRequest;
import com.example.NotsHub.payload.SemesterDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SemesterService {
    SemesterDTO createSemester(@Valid SemesterCreateRequest request);

    Page<SemesterDTO> getAllSemester(int page, int size);

    void deleteSemester(UUID id);

    SemesterDTO updateSemester(UUID id, @Valid SemesterCreateRequest request);
}
