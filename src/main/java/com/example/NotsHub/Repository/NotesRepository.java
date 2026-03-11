package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Notes;
import com.example.NotsHub.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotesRepository extends JpaRepository<Notes, UUID> {
    List<Notes> findBySubjectId(UUID subjectId);
    long countBySubjectId(UUID subjectId);
    boolean existsBySlug(String slug);
    Optional<Notes> findBySlug(String slug);
    Page<Notes> findBySubjectIdAndIsApprovedTrue(UUID subjectId, Pageable pageable);
    List<Notes> findBySubjectIdIn(List<UUID> subjectIds);
    Page<Notes> findByIsApprovedTrue(Pageable pageable);
    Page<Notes> findByIsApproved(Boolean isApproved, Pageable pageable);
    Page<Notes> findByIsApprovedFalseAndRejectionNoteIsNull(Pageable pageable);
    Page<Notes> findByIsApprovedFalseAndRejectionNoteIsNotNull(Pageable pageable);
    Page<Notes> findByUploadedBy(User uploadedBy, Pageable pageable);
    Page<Notes> findByUploadedByAndIsApproved(User uploadedBy, Boolean isApproved, Pageable pageable);
    Page<Notes> findByUploadedByAndIsApprovedTrue(User uploadedBy, Pageable pageable);
    Page<Notes> findByUploadedByAndIsApprovedFalseAndRejectionNoteIsNull(User uploadedBy, Pageable pageable);
    Page<Notes> findByUploadedByAndIsApprovedFalseAndRejectionNoteIsNotNull(User uploadedBy, Pageable pageable);
    Page<Notes> findByIsApprovedTrueAndTitleContainingIgnoreCaseOrIsApprovedTrueAndDescriptionContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByUploadedByAndTitleContainingIgnoreCaseOrUploadedByAndDescriptionContainingIgnoreCase(
            User uploaderForTitle,
            String titleKeyword,
            User uploaderForDescription,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByUploadedByAndIsApprovedAndTitleContainingIgnoreCaseOrUploadedByAndIsApprovedAndDescriptionContainingIgnoreCase(
            User uploaderForTitle,
            Boolean approvedForTitle,
            String titleKeyword,
            User uploaderForDescription,
            Boolean approvedForDescription,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByUploadedByAndIsApprovedTrueAndTitleContainingIgnoreCaseOrUploadedByAndIsApprovedTrueAndDescriptionContainingIgnoreCase(
            User uploaderForTitle,
            String titleKeyword,
            User uploaderForDescription,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByUploadedByAndIsApprovedFalseAndRejectionNoteIsNullAndTitleContainingIgnoreCaseOrUploadedByAndIsApprovedFalseAndRejectionNoteIsNullAndDescriptionContainingIgnoreCase(
            User uploaderForTitle,
            String titleKeyword,
            User uploaderForDescription,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByUploadedByAndIsApprovedFalseAndRejectionNoteIsNotNullAndTitleContainingIgnoreCaseOrUploadedByAndIsApprovedFalseAndRejectionNoteIsNotNullAndDescriptionContainingIgnoreCase(
            User uploaderForTitle,
            String titleKeyword,
            User uploaderForDescription,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword,
            Pageable pageable
    );
    Page<Notes> findByIsApprovedAndTitleContainingIgnoreCaseOrIsApprovedAndDescriptionContainingIgnoreCase(
            Boolean approvedForTitle,
            String titleKeyword,
            Boolean approvedForDescription,
            String descriptionKeyword,
            Pageable pageable
    );

    long countByUploadedByAndIsApprovedTrue(com.example.NotsHub.model.User uploadedBy);

    boolean existsByUploadedByAndSubjectIdAndFileChecksumAndIsApprovedFalseAndRejectionNoteIsNull(
            com.example.NotsHub.model.User uploadedBy,
            UUID subjectId,
            String fileChecksum
    );
}
