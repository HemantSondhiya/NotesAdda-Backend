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
import com.example.NotsHub.util.SlugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotesServiceImpl implements NotesService {

    @Autowired
    private NotesRepository notesRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private S3StorageService s3StorageService;

    @Autowired
    private PdfCompressionService pdfCompressionService;



    @Override
    public NotesDTO uploadPdfNote(String title, String description, UUID subjectId, MultipartFile file, String uploaderUsername) {
        if (title == null || title.isBlank()) {
            throw new APIException("Notes title is required");
        }
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new APIException("Subject not found with id: " + subjectId));

        User uploader = userRepository.findByUserName(uploaderUsername)
                .orElseThrow(() -> new APIException("User not found: " + uploaderUsername));

        boolean autoApprove = isAdmin(uploader);

        byte[] originalPdfBytes;
        try {
            originalPdfBytes = file.getBytes();
        } catch (Exception ex) {
            throw new APIException("Failed to read uploaded file");
        }

        byte[] compressedPdfBytes = pdfCompressionService.compress(originalPdfBytes);
        String checksum = calculateSha256Hex(compressedPdfBytes);

        if (!autoApprove) {
            boolean alreadyPending = notesRepository.existsByUploadedByAndSubjectIdAndFileChecksumAndIsApprovedFalse(
                    uploader, subjectId, checksum
            );
            if (alreadyPending) {
                throw new APIException("You have already uploaded this exact file and it is pending approval.");
            }
        }

        S3StorageService.UploadResult uploadResult;

        if (autoApprove) {
            // Admin: upload directly to notes/ prefix
            uploadResult = s3StorageService.uploadPdfBytes(compressedPdfBytes, uploaderUsername);
        } else {
            // Normal user: upload to pending/ prefix
            uploadResult = s3StorageService.uploadPdfToPendingBytes(compressedPdfBytes, uploaderUsername);
        }

        Notes notes = new Notes();
        notes.setTitle(title.trim());
        notes.setDescription(description);
        notes.setFileUrl(uploadResult.fileUrl());
        notes.setFileKey(uploadResult.fileKey());
        notes.setFileType("PDF");
        notes.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(title.trim()), notesRepository::existsBySlug));
        notes.setFileChecksum(checksum);
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
        notes.setRejectionNote(null);

        Notes saved = notesRepository.save(notes);
        return mapToDTO(saved);
    }

    @Override
    public NotesDTO rejectNotes(UUID notesId, String rejectionNote, String rejectorUsername) {
        Notes notes = notesRepository.findById(notesId)
                .orElseThrow(() -> new APIException("Notes not found with id: " + notesId));

        User rejector = userRepository.findByUserName(rejectorUsername)
                .orElseThrow(() -> new APIException("User not found: " + rejectorUsername));

        if (!isAdmin(rejector)) {
            throw new APIException("Only admin can reject notes");
        }

        notes.setIsApproved(false);
        notes.setRejectionNote(rejectionNote);
        notes.setApprovedBy(null);
        notes.setApprovedAt(null);
        
        // Delete the pending file from S3 to save space, since it's rejected
        if (notes.getFileKey() != null) {
            s3StorageService.deleteFile(notes.getFileKey());
            notes.setFileKey(null);
            notes.setFileUrl(null);
        }

        Notes saved = notesRepository.save(notes);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Page<NotesDTO> getAllNotes(int page, int size) {
        return notesRepository.findByIsApprovedTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public NotesDTO getBySlug(String slug) {
        Notes notes = notesRepository.findBySlug(slug)
                .orElseThrow(() -> new APIException("Notes not found with slug: " + slug));
        return mapToDTO(notes);
    }

    @Override
    @Transactional(readOnly = true)
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

    private String calculateSha256Hex(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new APIException("Failed to calculate file checksum");
        }
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
        dto.setSlug(notes.getSlug());
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
        if (notes.getId() != null) {
            dto.setDownloadUrl("/api/notes/" + notes.getId() + "/download");
        }
        if (notes.getUploadedBy() != null) {
            dto.setUploadedById(notes.getUploadedBy().getUserId());
            dto.setUploaderName(notes.getUploadedBy().getUserName());
            dto.setUploaderEmail(notes.getUploadedBy().getEmail());
            dto.setUploaderTotalNotes(notesRepository.countByUploadedByAndIsApprovedTrue(notes.getUploadedBy()));
        } else {
            dto.setUploaderTotalNotes(0L);
        }
        if (notes.getApprovedBy() != null) {
            dto.setApprovedById(notes.getApprovedBy().getUserId());
            dto.setApprovedByEmail(notes.getApprovedBy().getEmail());
        }

        return dto;
    }
}
