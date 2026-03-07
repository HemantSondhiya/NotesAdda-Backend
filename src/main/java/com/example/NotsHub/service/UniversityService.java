package com.example.NotsHub.service;

import com.example.NotsHub.payload.UniversityCreateRequest;
import com.example.NotsHub.payload.UniversityDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface UniversityService {
    UniversityDTO createUniversity(@Valid UniversityCreateRequest request);

    Page<UniversityDTO> getAllUniversities(int page, int size);
    
    UniversityDTO getBySlug(String slug);

    UniversityDTO updateUniversity(UUID id, @Valid UniversityCreateRequest request);

    void deleteUniversity(UUID id);
}
