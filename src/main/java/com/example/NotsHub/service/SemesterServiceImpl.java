package com.example.NotsHub.service;

import com.example.NotsHub.Repository.BranchRepository;
import com.example.NotsHub.Repository.SemesterRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Branch;
import com.example.NotsHub.model.Semester;
import com.example.NotsHub.payload.SemesterCreateRequest;
import com.example.NotsHub.payload.SemesterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Override
    public SemesterDTO createSemester(SemesterCreateRequest request) {
        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new APIException("Branch not found with id: " + request.getBranchId()));

        if (semesterRepository.existsByNumberAndBranchId(request.getNumber(), request.getBranchId())) {
            throw new APIException("Semester " + request.getNumber() + " already exists for this branch");
        }

        Semester semester = new Semester();
        semester.setNumber(request.getNumber());
        semester.setBranch(branch);

        Semester saved = semesterRepository.save(semester);

        SemesterDTO dto = new SemesterDTO();
        dto.setId(saved.getId());
        dto.setNumber(saved.getNumber());
        dto.setBranchId(saved.getBranch().getId());
        dto.setSubjects(new ArrayList<>());
        return dto;
    }

    @Override
    public Page<SemesterDTO> getAllSemester(int page, int size) {
        return semesterRepository.findAll(PageRequest.of(page, size))
                .map(semester -> {
                    SemesterDTO dto = new SemesterDTO();
                    dto.setId(semester.getId());
                    dto.setNumber(semester.getNumber());
                    dto.setBranchId(semester.getBranch().getId());
                    dto.setSubjects(new ArrayList<>());
                    return dto;
                });
    }

    @Override
    public void deleteSemester(UUID id) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new APIException("Semester not found with id: " + id));
        semesterRepository.delete(semester);
    }

    @Override
    public SemesterDTO updateSemester(UUID id, SemesterCreateRequest request) {
        Semester semester = semesterRepository.findById(id)
                .orElseThrow(() -> new APIException("Semester not found with id: " + id));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new APIException("Branch not found with id: " + request.getBranchId()));

        if (semesterRepository.existsByNumberAndBranchIdAndIdNot(request.getNumber(), request.getBranchId(), id)) {
            throw new APIException("Semester " + request.getNumber() + " already exists for this branch");
        }

        semester.setNumber(request.getNumber());
        semester.setBranch(branch);

        Semester updated = semesterRepository.save(semester);

        SemesterDTO dto = new SemesterDTO();
        dto.setId(updated.getId());
        dto.setNumber(updated.getNumber());
        dto.setBranchId(updated.getBranch().getId());
        dto.setSubjects(new ArrayList<>());
        return dto;
    }
}
