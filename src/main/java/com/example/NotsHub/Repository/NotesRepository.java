package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Notes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotesRepository extends JpaRepository<Notes, UUID> {
    List<Notes> findBySubjectId(UUID subjectId);
    Page<Notes> findBySubjectIdAndIsApprovedTrue(UUID subjectId, Pageable pageable);
    List<Notes> findBySubjectIdIn(List<UUID> subjectIds);
    Page<Notes> findByIsApprovedTrue(Pageable pageable);
    Page<Notes> findByIsApprovedTrueAndTitleContainingIgnoreCaseOrIsApprovedTrueAndDescriptionContainingIgnoreCase(
            String titleKeyword,
            String descriptionKeyword,
            Pageable pageable
    );
}
