package com.example.NotsHub.service;

import com.example.NotsHub.Repository.BranchRepository;
import com.example.NotsHub.Repository.NotesRepository;
import com.example.NotsHub.Repository.ProgramRepository;
import com.example.NotsHub.Repository.SemesterRepository;
import com.example.NotsHub.Repository.SubjectRepository;
import com.example.NotsHub.Repository.UniversityRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Branch;
import com.example.NotsHub.model.Notes;
import com.example.NotsHub.model.Program;
import com.example.NotsHub.model.Semester;
import com.example.NotsHub.model.Subject;
import com.example.NotsHub.model.University;
import com.example.NotsHub.payload.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UniversityServiceImpl implements UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private NotesRepository notesRepository;

    @Override
    public UniversityDTO createUniversity(UniversityCreateRequest request) {
        if (universityRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new APIException(
                    "University with code '" + request.getCode() + "' already exists");
        }

        University university = new University();
        university.setName(request.getName());
        university.setCode(request.getCode().toUpperCase());
        university.setCity(request.getCity());
        university.setState(request.getState());
        university.setLogoUrl(request.getLogoUrl());
        university.setIsActive(true);

        return mapToDTO(universityRepository.save(university));
    }

    @Override
    public Page<UniversityDTO> getAllUniversities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<University> universityPage = universityRepository.findByIsActiveTrue(pageable);
        List<University> universities = universityPage.getContent();

        if (universities.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, universityPage.getTotalElements());
        }

        List<UUID> universityIds = universities.stream().map(University::getId).toList();
        List<Program> programs = programRepository.findByUniversityIdIn(universityIds);
        List<UUID> programIds = programs.stream().map(Program::getId).toList();

        List<Branch> branches = programIds.isEmpty()
                ? Collections.emptyList()
                : branchRepository.findByProgramIdIn(programIds);
        List<UUID> branchIds = branches.stream().map(Branch::getId).toList();

        List<Semester> semesters = branchIds.isEmpty()
                ? Collections.emptyList()
                : semesterRepository.findByBranchIdIn(branchIds);
        List<UUID> semesterIds = semesters.stream().map(Semester::getId).toList();

        List<Subject> subjects = semesterIds.isEmpty()
                ? Collections.emptyList()
                : subjectRepository.findBySemesterIdIn(semesterIds);
        List<UUID> subjectIds = subjects.stream().map(Subject::getId).toList();

        List<Notes> notes = subjectIds.isEmpty()
                ? Collections.emptyList()
                : notesRepository.findBySubjectIdIn(subjectIds);

        Map<UUID, List<Program>> programsByUniversityId = programs.stream()
                .collect(Collectors.groupingBy(p -> p.getUniversity().getId()));
        Map<UUID, List<Branch>> branchesByProgramId = branches.stream()
                .collect(Collectors.groupingBy(b -> b.getProgram().getId()));
        Map<UUID, List<Semester>> semestersByBranchId = semesters.stream()
                .collect(Collectors.groupingBy(s -> s.getBranch().getId()));
        Map<UUID, List<Subject>> subjectsBySemesterId = subjects.stream()
                .collect(Collectors.groupingBy(s -> s.getSemester().getId()));
        Map<UUID, List<Notes>> notesBySubjectId = notes.stream()
                .collect(Collectors.groupingBy(n -> n.getSubject().getId()));

        List<UniversityDTO> result = universities.stream()
                .map(university -> mapUniversityToDTO(
                        university,
                        programsByUniversityId,
                        branchesByProgramId,
                        semestersByBranchId,
                        subjectsBySemesterId,
                        notesBySubjectId
                ))
                .toList();

        return new PageImpl<>(result, pageable, universityPage.getTotalElements());
    }

    @Override
    public UniversityDTO updateUniversity(UUID id, UniversityCreateRequest request) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new APIException("University not found with id: " + id));

        if (!university.getCode().equalsIgnoreCase(request.getCode()) &&
                universityRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new APIException(
                    "University with code '" + request.getCode() + "' already exists");
        }

        university.setName(request.getName());
        university.setCode(request.getCode().toUpperCase());
        university.setCity(request.getCity());
        university.setState(request.getState());
        university.setLogoUrl(request.getLogoUrl());

        return mapToDTO(universityRepository.save(university));
    }

    @Override
    public void deleteUniversity(UUID id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new APIException("University not found with id: " + id));

        university.setIsActive(false);
        universityRepository.save(university);
    }

    private UniversityDTO mapToDTO(University university) {
        UniversityDTO dto = new UniversityDTO();
        dto.setId(university.getId());
        dto.setName(university.getName());
        dto.setCode(university.getCode());
        dto.setCity(university.getCity());
        dto.setState(university.getState());
        dto.setLogoUrl(university.getLogoUrl());
        dto.setIsActive(university.getIsActive());
        dto.setCreatedAt(university.getCreatedAt());

        dto.setPrograms(new ArrayList<>());
        return dto;
    }

    private UniversityDTO mapUniversityToDTO(
            University university,
            Map<UUID, List<Program>> programsByUniversityId,
            Map<UUID, List<Branch>> branchesByProgramId,
            Map<UUID, List<Semester>> semestersByBranchId,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        UniversityDTO dto = mapToDTO(university);
        List<ProgramDTO> programDTOs = programsByUniversityId
                .getOrDefault(university.getId(), Collections.emptyList())
                .stream()
                .map(program -> mapProgramToDTO(
                        program,
                        branchesByProgramId,
                        semestersByBranchId,
                        subjectsBySemesterId,
                        notesBySubjectId
                ))
                .toList();
        dto.setPrograms(programDTOs);
        return dto;
    }

    private ProgramDTO mapProgramToDTO(
            Program program,
            Map<UUID, List<Branch>> branchesByProgramId,
            Map<UUID, List<Semester>> semestersByBranchId,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setType(program.getType());
        dto.setDuration(program.getDuration());

        if (program.getUniversity() != null) {
            dto.setUniversityId(program.getUniversity().getId());
        }

        List<BranchDTO> branchDTOs = branchesByProgramId
                .getOrDefault(program.getId(), Collections.emptyList())
                .stream()
                .map(branch -> mapBranchToDTO(branch, semestersByBranchId, subjectsBySemesterId, notesBySubjectId))
                .toList();
        dto.setBranches(branchDTOs);
        return dto;
    }

    private BranchDTO mapBranchToDTO(
            Branch branch,
            Map<UUID, List<Semester>> semestersByBranchId,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setCode(branch.getCode());

        if (branch.getProgram() != null) {
            dto.setProgramId(branch.getProgram().getId());
        }

        List<SemesterDTO> semesterDTOs = semestersByBranchId
                .getOrDefault(branch.getId(), Collections.emptyList())
                .stream()
                .map(semester -> mapToDTO(semester, subjectsBySemesterId, notesBySubjectId))
                .collect(Collectors.toList());

        dto.setSemesters(semesterDTOs);
        return dto;
    }

    private SemesterDTO mapToDTO(
            Semester semester,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        SemesterDTO dto = new SemesterDTO();
        dto.setId(semester.getId());
        dto.setNumber(semester.getNumber());

        if (semester.getBranch() != null) {
            dto.setBranchId(semester.getBranch().getId());
        }

        List<SubjectDTO> subjectDTOs = subjectsBySemesterId
                .getOrDefault(semester.getId(), Collections.emptyList())
                .stream()
                .map(subject -> mapToDTO(subject, notesBySubjectId))
                .collect(Collectors.toList());

        dto.setSubjects(subjectDTOs);
        return dto;
    }

    private SubjectDTO mapToDTO(Subject subject, Map<UUID, List<Notes>> notesBySubjectId) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setCode(subject.getCode());
        dto.setCredits(subject.getCredits());
        dto.setSyllabusUrl(subject.getSyllabusUrl());

        if (subject.getSemester() != null) {
            dto.setSemesterId(subject.getSemester().getId());
        }

        List<NotesDTO> notesDTOs = notesBySubjectId
                .getOrDefault(subject.getId(), Collections.emptyList())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        dto.setNotes(notesDTOs);
        return dto;
    }

    private NotesDTO mapToDTO(Notes notes) {
        NotesDTO dto = new NotesDTO();
        dto.setId(notes.getId());
        dto.setTitle(notes.getTitle());
        dto.setDescription(notes.getDescription());
        dto.setFileUrl(notes.getFileUrl());
        dto.setFileKey(notes.getFileKey());
        dto.setFileType(notes.getFileType());
        dto.setIsApproved(notes.getIsApproved());
        dto.setRejectionNote(notes.getRejectionNote());
        dto.setCreatedAt(notes.getCreatedAt());
        dto.setApprovedAt(notes.getApprovedAt());

        if (notes.getSubject() != null) {
            dto.setSubjectId(notes.getSubject().getId());
        }
        if (notes.getUploadedBy() != null) {
            dto.setUploadedById(notes.getUploadedBy().getUserId());
        }
        if (notes.getApprovedBy() != null) {
            dto.setApprovedById(notes.getApprovedBy().getUserId());
        }

        return dto;
    }
}
