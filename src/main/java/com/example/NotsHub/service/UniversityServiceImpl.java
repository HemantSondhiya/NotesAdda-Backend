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
import com.example.NotsHub.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    private S3StorageService s3StorageService;

    @Override
    public UniversityDTO createUniversity(UniversityCreateRequest request, MultipartFile logoFile) {
        if (universityRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new APIException(
                    "University with code '" + request.getCode() + "' already exists");
        }

        University university = new University();
        university.setName(request.getName());
        university.setCode(request.getCode().toUpperCase());
        university.setDescription(request.getDescription());
        university.setCity(request.getCity());
        university.setState(request.getState());
        if (logoFile != null && !logoFile.isEmpty()) {
            S3StorageService.UploadResult result = s3StorageService.uploadImage(logoFile, "universities/logos");
            university.setLogoUrl(result.fileUrl());
        } else {
            university.setLogoUrl(request.getLogoUrl());
        }
        university.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(request.getName()), universityRepository::existsBySlug));
        university.setIsActive(true);

        return mapToDTO(universityRepository.save(university));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UniversityDTO> getAllUniversities(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<University> universityPage = universityRepository.findByIsActiveTrue(pageable);
        return universityPage.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UniversityDTO getBySlug(String slug) {
        University university = universityRepository.findBySlug(slug)
                .orElseThrow(() -> new APIException("University not found with slug: " + slug));
        return mapToDTO(university);
    }

    @Override
    public UniversityDTO updateUniversity(UUID id, UniversityCreateRequest request, MultipartFile logoFile) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new APIException("University not found with id: " + id));

        if (!university.getCode().equalsIgnoreCase(request.getCode()) &&
                universityRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new APIException(
                    "University with code '" + request.getCode() + "' already exists");
        }

        university.setName(request.getName());
        university.setCode(request.getCode().toUpperCase());
        university.setDescription(request.getDescription());
        university.setCity(request.getCity());
        university.setState(request.getState());

        String previousLogoUrl = university.getLogoUrl();
        String newUploadedLogoKey = null;

        try {
            if (logoFile != null && !logoFile.isEmpty()) {
                S3StorageService.UploadResult result = s3StorageService.uploadImage(logoFile, "universities/logos");
                newUploadedLogoKey = result.fileKey();
                university.setLogoUrl(result.fileUrl());
            } else if (request.getLogoUrl() != null) {
                university.setLogoUrl(request.getLogoUrl());
            }

            University saved = universityRepository.save(university);
            deleteReplacedLogo(previousLogoUrl, saved.getLogoUrl());
            return mapToDTO(saved);
        } catch (RuntimeException ex) {
            if (newUploadedLogoKey != null) {
                s3StorageService.deleteFile(newUploadedLogoKey);
            }
            throw ex;
        }
    }

    @Override
    public UniversityDTO uploadLogo(UUID id, MultipartFile logoFile) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new APIException("University not found with id: " + id));

        if (logoFile == null || logoFile.isEmpty()) {
            throw new APIException("Logo file is required");
        }

        String previousLogoUrl = university.getLogoUrl();
        S3StorageService.UploadResult result = s3StorageService.uploadImage(logoFile, "universities/logos");
        String newUploadedLogoKey = result.fileKey();
        university.setLogoUrl(result.fileUrl());

        try {
            University saved = universityRepository.save(university);
            deleteReplacedLogo(previousLogoUrl, saved.getLogoUrl());
            return mapToDTO(saved);
        } catch (RuntimeException ex) {
            s3StorageService.deleteFile(newUploadedLogoKey);
            throw ex;
        }
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
        dto.setSlug(university.getSlug());
        dto.setCode(university.getCode());
        dto.setDescription(university.getDescription());
        dto.setCity(university.getCity());
        dto.setState(university.getState());
        dto.setLogoUrl(university.getLogoUrl());
        dto.setIsActive(university.getIsActive());
        dto.setCreatedAt(university.getCreatedAt());
        dto.setProgramsCountTotal(programRepository.countByUniversityId(university.getId()));

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
                        university,
                        program,
                        branchesByProgramId,
                        semestersByBranchId,
                        subjectsBySemesterId,
                        notesBySubjectId
                ))
                .toList();
        dto.setProgramsCountTotal((long) programDTOs.size());
        dto.setPrograms(programDTOs);
        return dto;
    }

    private ProgramDTO mapProgramToDTO(
            University university,
            Program program,
            Map<UUID, List<Branch>> branchesByProgramId,
            Map<UUID, List<Semester>> semestersByBranchId,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        ProgramDTO dto = new ProgramDTO();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setDescription(program.getDescription());
        dto.setType(program.getType());
        dto.setDuration(program.getDuration());

        dto.setUniversityId(university.getId());

        List<BranchDTO> branchDTOs = branchesByProgramId
                .getOrDefault(program.getId(), Collections.emptyList())
                .stream()
                .map(branch -> mapBranchToDTO(
                        university,
                        program,
                        branch,
                        semestersByBranchId,
                        subjectsBySemesterId,
                        notesBySubjectId
                ))
                .toList();
        dto.setBranchesCountTotal((long) branchDTOs.size());
        dto.setBranches(branchDTOs);
        return dto;
    }

    private BranchDTO mapBranchToDTO(
            University university,
            Program program,
            Branch branch,
            Map<UUID, List<Semester>> semestersByBranchId,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setCode(branch.getCode());
        dto.setDescription(branch.getDescription());
        dto.setProgramId(program.getId());

        List<SemesterDTO> semesterDTOs = semestersByBranchId
                .getOrDefault(branch.getId(), Collections.emptyList())
                .stream()
                .map(semester -> mapToDTO(
                        semester,
                        branch,
                        program,
                        university,
                        subjectsBySemesterId,
                        notesBySubjectId
                ))
                .collect(Collectors.toList());

        dto.setSemestersCountTotal((long) semesterDTOs.size());
        dto.setSemesters(semesterDTOs);
        return dto;
    }

    private SemesterDTO mapToDTO(
            Semester semester,
            Branch branch,
            Program program,
            University university,
            Map<UUID, List<Subject>> subjectsBySemesterId,
            Map<UUID, List<Notes>> notesBySubjectId
    ) {
        SemesterDTO dto = new SemesterDTO();
        dto.setId(semester.getId());
        dto.setNumber(semester.getNumber());
        if (semester.getNumber() != null) {
            dto.setSemester("Semester " + semester.getNumber());
        }

        if (branch != null) {
            dto.setBranchId(branch.getId());
            dto.setBranchName(branch.getName());
            dto.setBranchCode(branch.getCode());
        }
        dto.setProgramId(program.getId());
        dto.setProgramName(program.getName());
        dto.setUniversityId(university.getId());
        dto.setUniversityName(university.getName());

        List<SubjectDTO> subjectDTOs = subjectsBySemesterId
                .getOrDefault(semester.getId(), Collections.emptyList())
                .stream()
                .map(subject -> mapToDTO(subject, notesBySubjectId))
                .collect(Collectors.toList());

        dto.setSubjectsCountTotal((long) subjectDTOs.size());
        dto.setSubjects(subjectDTOs);
        return dto;
    }

    private SubjectDTO mapToDTO(Subject subject, Map<UUID, List<Notes>> notesBySubjectId) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setCode(subject.getCode());
        dto.setDescription(subject.getDescription());

        if (subject.getSemester() != null) {
            dto.setSemesterId(subject.getSemester().getId());
        }

        List<NotesDTO> notesDTOs = notesBySubjectId
                .getOrDefault(subject.getId(), Collections.emptyList())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        dto.setNotesCountTotal((long) notesDTOs.size());
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

    private void deleteReplacedLogo(String previousLogoUrl, String newLogoUrl) {
        if (Objects.equals(previousLogoUrl, newLogoUrl)) {
            return;
        }

        String previousKey = s3StorageService.extractManagedKeyFromS3Url(previousLogoUrl);
        String newKey = s3StorageService.extractManagedKeyFromS3Url(newLogoUrl);

        if (previousKey != null && !previousKey.equals(newKey)) {
            s3StorageService.deleteFile(previousKey);
        }
    }
}
