package com.example.NotsHub.service;

import com.example.NotsHub.payload.ProgramCreateRequest;
import com.example.NotsHub.payload.ProgramDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ProgramService {

    ProgramDTO createProgram(ProgramCreateRequest request);

    Page<ProgramDTO> getProgramsByUniversity(UUID universityId, int page, int size);

    Page<ProgramDTO> getAllPrograms(int page, int size);

    ProgramDTO getProgramById(UUID programId);

    ProgramDTO updateProgram(UUID programId, ProgramCreateRequest request);

    void deleteProgram(UUID programId);
}
