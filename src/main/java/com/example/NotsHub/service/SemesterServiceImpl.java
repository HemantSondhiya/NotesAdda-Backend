package com.example.NotsHub.service;

import com.example.NotsHub.Repository.BranchRepository;
import com.example.NotsHub.Repository.SemesterRepository;
import com.example.NotsHub.Repository.SubjectRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Branch;
import com.example.NotsHub.model.Semester;
import com.example.NotsHub.payload.SemesterCreateRequest;
import com.example.NotsHub.payload.SemesterDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class SemesterServiceImpl implements SemesterService {

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private SubjectRepository subjectRepository;

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
        Semester savedWithHierarchy = semesterRepository.findById(saved.getId())
                .orElse(saved);
        return mapToDTO(savedWithHierarchy);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SemesterDTO> getAllSemester(int page, int size) {
        return semesterRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SemesterDTO> getSemestersByBranch(UUID branchId, int page, int size) {
        if (!branchRepository.existsById(branchId)) {
            throw new APIException("Branch not found with id: " + branchId);
        }
        return semesterRepository.findByBranchId(branchId, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "number")))
                .map(semester -> mapToSummaryDTO(semester, branchId));
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
        Semester updatedWithHierarchy = semesterRepository.findById(updated.getId())
                .orElse(updated);
        return mapToDTO(updatedWithHierarchy);
    }

    private SemesterDTO mapToDTO(Semester semester) {
        SemesterDTO dto = new SemesterDTO();
        dto.setId(semester.getId());
        dto.setNumber(semester.getNumber());
        if (semester.getNumber() != null) {
            dto.setSemester("Semester " + semester.getNumber());
        }
        if (semester.getBranch() != null) {
            dto.setBranchId(semester.getBranch().getId());
            dto.setBranchName(semester.getBranch().getName());
            dto.setBranchCode(semester.getBranch().getCode());
            if (semester.getBranch().getProgram() != null) {
                dto.setProgramId(semester.getBranch().getProgram().getId());
                dto.setProgramName(semester.getBranch().getProgram().getName());
                if (semester.getBranch().getProgram().getUniversity() != null) {
                    dto.setUniversityId(semester.getBranch().getProgram().getUniversity().getId());
                    dto.setUniversityName(semester.getBranch().getProgram().getUniversity().getName());
                }
            }
        }
        dto.setSubjectsCountTotal(subjectRepository.countBySemesterId(semester.getId()));
        dto.setSubjects(new ArrayList<>());
        return dto;
    }

    private SemesterDTO mapToSummaryDTO(Semester semester, UUID branchId) {
        SemesterDTO dto = new SemesterDTO();
        dto.setId(semester.getId());
        dto.setNumber(semester.getNumber());
        if (semester.getNumber() != null) {
            dto.setSemester("Semester " + semester.getNumber());
        }
        dto.setBranchId(branchId);
        dto.setSubjectsCountTotal(subjectRepository.countBySemesterId(semester.getId()));
        dto.setSubjects(new ArrayList<>());
        return dto;
    }
}
