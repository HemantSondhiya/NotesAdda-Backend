package com.example.NotsHub.service;

import com.example.NotsHub.Repository.ProgramRepository;
import com.example.NotsHub.Repository.UniversityRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Branch;
import com.example.NotsHub.model.Program;
import com.example.NotsHub.model.University;
import com.example.NotsHub.payload.BranchDTO;
import com.example.NotsHub.payload.ProgramCreateRequest;
import com.example.NotsHub.payload.ProgramDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProgramServiceImpl implements ProgramService {

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Override
    public ProgramDTO createProgram(ProgramCreateRequest request) {

        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new APIException("University not found with id: " + request.getUniversityId()));

        if (programRepository.existsByNameAndUniversityId(request.getName(), request.getUniversityId())) {
            throw new APIException(
                    "Program '" + request.getName() + "' already exists in this university");
        }

        Program program = new Program();
        program.setName(request.getName());
        program.setType(request.getType());
        program.setDuration(request.getDuration());
        program.setUniversity(university);

        Program saved = programRepository.save(program);
        Program savedWithBranches = programRepository.findByIdWithBranches(saved.getId())
                .orElseThrow(() -> new APIException("Failed to create program"));
        return mapToDTO(savedWithBranches);
    }

    @Override
    public Page<ProgramDTO> getProgramsByUniversity(UUID universityId, int page, int size) {

        if (!universityRepository.existsById(universityId)) {
            throw new APIException("University not found with id: " + universityId);
        }

        Page<Program> programPage = programRepository.findByUniversityId(
                universityId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        return toProgramDTOPage(programPage);
    }

    @Override
    public Page<ProgramDTO> getAllPrograms(int page, int size) {
        Page<Program> programPage = programRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        );
        return toProgramDTOPage(programPage);
    }

    @Override
    public ProgramDTO getProgramById(UUID programId) {
        Program program = programRepository.findByIdWithBranches(programId)
                .orElseThrow(() -> new APIException("Program not found with id: " + programId));
        return mapToDTO(program);
    }

    @Override
    public ProgramDTO updateProgram(UUID programId, ProgramCreateRequest request) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new APIException("Program not found with id: " + programId));

        University university;
        if (!program.getUniversity().getId().equals(request.getUniversityId())) {
            university = universityRepository.findById(request.getUniversityId())
                    .orElseThrow(() -> new APIException("University not found with id: " + request.getUniversityId()));
            program.setUniversity(university);
        }

        if (!program.getName().equalsIgnoreCase(request.getName())) {
            if (programRepository.existsByNameAndUniversityId(request.getName(), request.getUniversityId())) {
                throw new APIException(
                        "Program '" + request.getName() + "' already exists in this university");
            }
        }

        program.setName(request.getName());
        program.setType(request.getType());
        program.setDuration(request.getDuration());

        Program updated = programRepository.save(program);
        Program updatedWithBranches = programRepository.findByIdWithBranches(updated.getId())
                .orElseThrow(() -> new APIException("Failed to update program"));
        return mapToDTO(updatedWithBranches);
    }

    @Override
    public void deleteProgram(UUID programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new APIException("Program not found with id: " + programId));

        programRepository.delete(program);
    }

    private ProgramDTO mapToDTO(Program program) {
        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setType(program.getType());
        dto.setDuration(program.getDuration());
        dto.setUniversityId(program.getUniversity().getId());

        List<BranchDTO> branchDTOs = new ArrayList<>();
        if (program.getBranches() != null && !program.getBranches().isEmpty()) {
            branchDTOs = program.getBranches().stream()
                    .map(this::mapBranchToDTO)
                    .collect(Collectors.toList());
        }
        dto.setBranches(branchDTOs);

        return dto;
    }

    private BranchDTO mapBranchToDTO(Branch branch) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setCode(branch.getCode());

        if (branch.getProgram() != null) {
            dto.setProgramId(branch.getProgram().getId());
        }

        dto.setSemesters(new ArrayList<>());
        return dto;
    }

    private Page<ProgramDTO> toProgramDTOPage(Page<Program> programPage) {
        List<Program> programs = programPage.getContent();
        if (programs.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), programPage.getPageable(), programPage.getTotalElements());
        }

        List<UUID> ids = programs.stream().map(Program::getId).toList();
        Map<UUID, Program> fetchedById = programRepository.findProgramsWithBranches(ids)
                .stream()
                .collect(Collectors.toMap(Program::getId, Function.identity()));

        List<ProgramDTO> dtos = ids.stream()
                .map(fetchedById::get)
                .filter(p -> p != null)
                .map(this::mapToDTO)
                .toList();

        return new PageImpl<>(dtos, programPage.getPageable(), programPage.getTotalElements());
    }
}
