package com.example.NotsHub.service;

import com.example.NotsHub.payload.SubjectCreateRequest;
import com.example.NotsHub.payload.SubjectDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface SubjectService {
    SubjectDTO createSubject(@Valid SubjectCreateRequest request);

    Page<SubjectDTO> getSubjects(int page, int size);
    Page<SubjectDTO> getSubjectsBySemester(UUID semesterId, int page, int size);
    SubjectDTO getSubjectById(UUID id);

    void deleteSubject(UUID id);

    SubjectDTO updateSubject(UUID id, @Valid SubjectCreateRequest request);
}
