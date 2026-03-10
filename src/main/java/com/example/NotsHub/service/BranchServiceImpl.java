package com.example.NotsHub.service;

import com.example.NotsHub.Repository.BranchRepository;
import com.example.NotsHub.Repository.ProgramRepository;
import com.example.NotsHub.Repository.SemesterRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Branch;
import com.example.NotsHub.model.Program;
import com.example.NotsHub.model.Semester;
import com.example.NotsHub.payload.BranchCreateRequest;
import com.example.NotsHub.payload.BranchDTO;
import com.example.NotsHub.payload.SemesterDTO;
import com.example.NotsHub.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BranchServiceImpl implements BranchService {

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    // ================= CREATE BRANCH =================

    @Override
    public BranchDTO createBranch(BranchCreateRequest request) {

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() ->
                        new APIException("Program not found with id: " + request.getProgramId())
                );

        // Prevent duplicate branch inside same program
        if (branchRepository.existsByNameAndProgramId(
                request.getName(),
                request.getProgramId()
        )) {
            throw new APIException(
                    "Branch '" + request.getName() + "' already exists in this program"
            );
        }

        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setCode(request.getCode());
        branch.setProgram(program);

        Branch savedBranch = branchRepository.save(branch);

        return mapToDTO(savedBranch);
    }

    @Override
    public BranchDTO getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new APIException("Branch not found with id: " + id));
        return mapToDTO(branch);
    }

    @Override
    public BranchDTO getBySlug(String slug) {
        Branch branch = branchRepository.findBySlug(slug)
                .orElseThrow(() -> new APIException("Branch not found with slug: " + slug));
        return mapToDTO(branch);
    }

    @Override
    public BranchDTO updateBranch(UUID id, BranchCreateRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new APIException("Branch not found with id: " + id));

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new APIException("Program not found with id: " + request.getProgramId()));

        Branch existing = branchRepository.findByNameAndProgramId(request.getName(), request.getProgramId());
        if (existing != null && !existing.getId().equals(branch.getId())) {
            throw new APIException(
                    "Branch '" + request.getName() + "' already exists in this program"
            );
        }

        branch.setName(request.getName());
        branch.setCode(request.getCode());
        branch.setProgram(program);

        Branch updated = branchRepository.save(branch);
        return mapToDTO(updated);
    }

    @Override
    public void deleteBranch(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new APIException("Branch not found with id: " + id));
        branchRepository.delete(branch);
    }

    @Override
    public Page<BranchDTO> getAllBranches(int page, int size) {
        Page<Branch> branchPage = branchRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return branchPage.map(this::mapToDTO);
    }

    @Override
    public Page<BranchDTO> getBranchesByProgram(UUID programId, int page, int size) {
        if (!programRepository.existsById(programId)) {
            throw new APIException("Program not found with id: " + programId);
        }
        Page<Branch> branchPage = branchRepository.findByProgramId(
                programId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        return branchPage.map(this::mapToSummaryDTO);
    }

    // ================= BRANCH → DTO =================

    private BranchDTO mapToDTO(Branch branch) {

        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setSlug(branch.getSlug());
        dto.setCode(branch.getCode());
        dto.setProgramId(branch.getProgram().getId());

        List<SemesterDTO> semesterDTOs = new ArrayList<>();

        List<Semester> semesters = semesterRepository.findByBranchId(branch.getId());
        if (semesters != null && !semesters.isEmpty()) {
            semesterDTOs = semesters
                    .stream()
                    .map(this::mapSemesterToDTO)
                    .collect(Collectors.toList());
        }

        dto.setSemestersCountTotal((long) semesterDTOs.size());
        dto.setSemesters(semesterDTOs);

        return dto;
    }

    // ================= SEMESTER → DTO =================

    private SemesterDTO mapSemesterToDTO(Semester semester) {

        SemesterDTO dto = new SemesterDTO();

        dto.setId(semester.getId());
        dto.setNumber(semester.getNumber());
        dto.setBranchId(semester.getBranch().getId());

        // Avoid infinite recursion (Semester -> Subject -> Semester)
        dto.setSubjects(new ArrayList<>());

        return dto;
    }

    private BranchDTO mapToSummaryDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setSlug(branch.getSlug());
        dto.setCode(branch.getCode());
        dto.setProgramId(branch.getProgram().getId());
        dto.setSemestersCountTotal(semesterRepository.countByBranchId(branch.getId()));
        dto.setSemesters(new ArrayList<>());
        return dto;
    }
}
