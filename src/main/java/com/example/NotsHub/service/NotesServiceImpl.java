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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class NotesServiceImpl implements NotesService {

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3StorageService s3StorageService;

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
        notes.setFileType(normalizeAndValidatePdfFileType(request.getFileType()));
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
    public NotesDTO uploadPdfNote(String title, String description, UUID subjectId, MultipartFile file, String uploaderUsername) {
        if (title == null || title.isBlank()) {
            throw new APIException("Notes title is required");
        }
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new APIException("Subject not found with id: " + subjectId));

        User uploader = userRepository.findByUserName(uploaderUsername)
                .orElseThrow(() -> new APIException("User not found: " + uploaderUsername));

        S3StorageService.UploadResult uploadResult = s3StorageService.uploadPdf(file, uploaderUsername);

        Notes notes = new Notes();
        notes.setTitle(title.trim());
        notes.setDescription(description);
        notes.setFileUrl(uploadResult.fileUrl());
        notes.setFileKey(uploadResult.fileKey());
        notes.setFileType("PDF");
        notes.setSubject(subject);
        notes.setUploadedBy(uploader);
        if (isAdmin(uploader)) {
            notes.setIsApproved(true);
            notes.setApprovedBy(uploader);
            notes.setApprovedAt(LocalDateTime.now());
        } else {
            notes.setIsApproved(false);
        }

        try {
            Notes saved = notesRepository.save(notes);
            return mapToDTO(saved);
        } catch (RuntimeException ex) {
            s3StorageService.deleteFile(uploadResult.fileKey());
            throw ex;
        }
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
        notes.setFileType(normalizeAndValidatePdfFileType(request.getFileType()));
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
    public Map<String, String> generateDownloadLink(UUID notesId, @Nullable String requesterUsername) {
        Notes notes = notesRepository.findById(notesId)
                .orElseThrow(() -> new APIException("Notes not found with id: " + notesId));

        if (Boolean.TRUE.equals(notes.getIsApproved())) {
            String downloadName = notes.getTitle() == null ? "notes.pdf" : notes.getTitle() + ".pdf";
            String downloadUrl = s3StorageService.createPresignedDownloadUrl(notes.getFileKey(), downloadName);
            return Map.of(
                    "downloadUrl", downloadUrl,
                    "expiresInMinutes", String.valueOf(s3StorageService.getPresignedExpiryMinutes())
            );
        }

        if (requesterUsername == null || requesterUsername.isBlank()) {
            throw new APIException("Only approved notes are available for public download");
        }

        User requester = userRepository.findByUserName(requesterUsername)
                .orElseThrow(() -> new APIException("User not found: " + requesterUsername));

        boolean isUploader = notes.getUploadedBy() != null
                && notes.getUploadedBy().getUserId().equals(requester.getUserId());
        boolean isAllowed = isUploader || isAdmin(requester);

        if (!isAllowed) {
            throw new APIException("You are not allowed to download this note");
        }

        String downloadName = notes.getTitle() == null ? "notes.pdf" : notes.getTitle() + ".pdf";
        String downloadUrl = s3StorageService.createPresignedDownloadUrl(notes.getFileKey(), downloadName);

        return Map.of(
                "downloadUrl", downloadUrl,
                "expiresInMinutes", String.valueOf(s3StorageService.getPresignedExpiryMinutes())
        );
    }

    @Override
    public Page<NotesDTO> getAllNotes(int page, int size) {
        return notesRepository.findByIsApprovedTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToDTO);
    }

    @Override
    public Page<NotesDTO> getNotesBySubject(UUID subjectId, int page, int size) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new APIException("Subject not found with id: " + subjectId);
        }
        return notesRepository.findBySubjectIdAndIsApprovedTrue(
                        subjectId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
                .map(this::mapToDTO);
    }

    @Override
    public Page<NotesDTO> searchNotes(String query, int page, int size) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return getAllNotes(page, size);
        }

        return notesRepository.findByIsApprovedTrueAndTitleContainingIgnoreCaseOrIsApprovedTrueAndDescriptionContainingIgnoreCase(
                        normalizedQuery,
                        normalizedQuery,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
                )
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
        s3StorageService.deleteFile(notes.getFileKey());
        notesRepository.delete(notes);
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .map(Role::getRoleName)
                .anyMatch(role -> role == AppRole.ROLE_UNIVERSITY_ADMIN || role == AppRole.ROLE_SUPER_ADMIN);
    }

    private String normalizeAndValidatePdfFileType(String fileType) {
        if (fileType == null || fileType.isBlank()) {
            throw new APIException("File type is required");
        }
        String normalized = fileType.trim().toUpperCase();
        if (!"PDF".equals(normalized)) {
            throw new APIException("Only PDF file type is allowed");
        }
        return normalized;
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
