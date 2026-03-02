package com.example.NotsHub.service;

import com.example.NotsHub.Repository.NotesRepository;
import com.example.NotsHub.Repository.SubjectRepository;
import com.example.NotsHub.Repository.UserRepository;
import com.example.NotsHub.exceptions.APIException;
import com.example.NotsHub.model.AppRole;
import com.example.NotsHub.model.Notes;
import com.example.NotsHub.model.Role;
import com.example.NotsHub.model.Subject;
import com.example.NotsHub.model.User;
import com.example.NotsHub.payload.NotesCreateRequest;
import com.example.NotsHub.payload.NotesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotesServiceImpl implements NotesService {

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public NotesDTO createNotes(NotesCreateRequest request, String uploaderUsername) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new APIException("Subject not found with id: " + request.getSubjectId()));

        User uploader = userRepository.findByUserName(uploaderUsername)
                .orElseThrow(() -> new APIException("User not found: " + uploaderUsername));

        Notes notes = new Notes();
        notes.setTitle(request.getTitle());
        notes.setDescription(request.getDescription());
        notes.setFileUrl(request.getFileUrl());
        notes.setFileKey(request.getFileKey());
        notes.setFileType(request.getFileType().trim().toUpperCase());
        notes.setSubject(subject);
        notes.setUploadedBy(uploader);
        if (isAdmin(uploader)) {
            notes.setIsApproved(true);
            notes.setApprovedBy(uploader);
            notes.setApprovedAt(LocalDateTime.now());
        } else {
            notes.setIsApproved(false);
        }

        Notes saved = notesRepository.save(notes);
        return mapToDTO(saved);
    }

    @Override
    public NotesDTO updateNotes(UUID notesId, NotesCreateRequest request, String updaterUsername) {
        Notes notes = notesRepository.findById(notesId)
                .orElseThrow(() -> new APIException("Notes not found with id: " + notesId));

        User updater = userRepository.findByUserName(updaterUsername)
                .orElseThrow(() -> new APIException("User not found: " + updaterUsername));

        if (!isAdmin(updater)) {
            throw new APIException("Only admin can update notes");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new APIException("Subject not found with id: " + request.getSubjectId()));

        notes.setTitle(request.getTitle());
        notes.setDescription(request.getDescription());
        notes.setFileUrl(request.getFileUrl());
        notes.setFileKey(request.getFileKey());
        notes.setFileType(request.getFileType().trim().toUpperCase());
        notes.setSubject(subject);

        notes.setIsApproved(true);
        notes.setApprovedBy(updater);
        notes.setApprovedAt(LocalDateTime.now());
        notes.setRejectionNote(null);

        Notes saved = notesRepository.save(notes);
        return mapToDTO(saved);
    }

    @Override
    public NotesDTO approveNotes(UUID notesId, String approverUsername) {
        Notes notes = notesRepository.findById(notesId)
                .orElseThrow(() -> new APIException("Notes not found with id: " + notesId));

        User approver = userRepository.findByUserName(approverUsername)
                .orElseThrow(() -> new APIException("User not found: " + approverUsername));

        if (!isAdmin(approver)) {
            throw new APIException("Only admin can approve notes");
        }

        notes.setIsApproved(true);
        notes.setApprovedBy(approver);
        notes.setApprovedAt(LocalDateTime.now());

        Notes saved = notesRepository.save(notes);
        return mapToDTO(saved);
    }

    @Override
    public Page<NotesDTO> getAllNotes(int page, int size) {
        return notesRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToDTO);
    }

    @Override
    public void deleteNotes(UUID notesId, String deleterUsername) {
        User deleter = userRepository.findByUserName(deleterUsername)
                .orElseThrow(() -> new APIException("User not found: " + deleterUsername));
        if (!isAdmin(deleter)) {
            throw new APIException("Only admin can delete notes");
        }

        Notes notes = notesRepository.findById(notesId)
                .orElseThrow(() -> new APIException("Notes not found with id: " + notesId));
        notesRepository.delete(notes);
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .map(Role::getRoleName)
                .anyMatch(role -> role == AppRole.ROLE_UNIVERSITY_ADMIN || role == AppRole.ROLE_SUPER_ADMIN);
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
