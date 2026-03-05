package com.example.NotsHub.service;

import com.example.NotsHub.payload.BranchCreateRequest;
import com.example.NotsHub.payload.BranchDTO;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BranchService {
    BranchDTO createBranch(@Valid BranchCreateRequest request);
    BranchDTO getBranchById(UUID id);
    BranchDTO updateBranch(UUID id, @Valid BranchCreateRequest request);
    void deleteBranch(UUID id);

    Page<BranchDTO> getAllBranches(int page, int size);
    Page<BranchDTO> getBranchesByProgram(UUID programId, int page, int size);
}
