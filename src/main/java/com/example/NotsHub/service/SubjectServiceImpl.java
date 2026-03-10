package com.example.NotsHub.service;

import com.example.NotsHub.Repository.SemesterRepository;
import com.example.NotsHub.Repository.NotesRepository;
import com.example.NotsHub.Repository.SubjectRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.Semester;
import com.example.NotsHub.model.Subject;
import com.example.NotsHub.payload.SubjectCreateRequest;
import com.example.NotsHub.payload.SubjectDTO;
import com.example.NotsHub.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private NotesRepository notesRepository;

    @Override
    public SubjectDTO createSubject(SubjectCreateRequest request) {
        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new APIException("Semester not found with id: " + request.getSemesterId()));

        if (subjectRepository.existsByCodeAndSemesterId(request.getCode(), request.getSemesterId())) {
            throw new APIException("Subject code '" + request.getCode() + "' already exists in this semester");
        }

        Subject subject = new Subject();
        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(request.getName()), subjectRepository::existsBySlug));
        subject.setCredits(request.getCredits());
        subject.setSyllabusUrl(request.getSyllabusUrl());
        subject.setSemester(semester);

        Subject saved = subjectRepository.save(subject);
        return mapToDTO(saved);
    }

    @Override
    public Page<SubjectDTO> getSubjects(int page, int size) {
        return subjectRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .map(this::mapToDTO);
    }

    @Override
    public Page<SubjectDTO> getSubjectsBySemester(UUID semesterId, int page, int size) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new APIException("Semester not found with id: " + semesterId);
        }

        return subjectRepository.findBySemesterId(semesterId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))
                .map(this::mapToDTO);
    }

    @Override
    public SubjectDTO getSubjectById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new APIException("Subject not found with id: " + id));
        return mapToDTO(subject);
    }

    @Override
    public SubjectDTO getBySlug(String slug) {
        Subject subject = subjectRepository.findBySlug(slug)
                .orElseThrow(() -> new APIException("Subject not found with slug: " + slug));
        return mapToDTO(subject);
    }

    @Override
    public void deleteSubject(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(()-> new APIException("Subject not found with id: " + id));
        subjectRepository.delete(subject);
    }

    @Override
    public SubjectDTO updateSubject(UUID id, SubjectCreateRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new APIException("Subject not found with id: " + id));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new APIException("Semester not found with id: " + request.getSemesterId()));

        if (!subject.getCode().equals(request.getCode()) &&
                subjectRepository.existsByCodeAndSemesterId(request.getCode(), request.getSemesterId())) {
            throw new APIException("Subject code '" + request.getCode() + "' already exists in this semester");
        }

        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setCredits(request.getCredits());
        subject.setSyllabusUrl(request.getSyllabusUrl());
        subject.setSemester(semester);

        Subject updated = subjectRepository.save(subject);
        return mapToDTO(updated);
    }

    private SubjectDTO mapToDTO(Subject saved) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(saved.getId());
        dto.setName(saved.getName());
        dto.setSlug(saved.getSlug());
        dto.setCode(saved.getCode());
        dto.setCredits(saved.getCredits());
        dto.setSyllabusUrl(saved.getSyllabusUrl());
        dto.setSemesterId(saved.getSemester().getId());
        dto.setNotesCountTotal(notesRepository.countBySubjectId(saved.getId()));
        dto.setNotes(new ArrayList<>());
        return dto;
    }
}
