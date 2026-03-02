package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Notes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotesRepository extends JpaRepository<Notes, UUID> {
    List<Notes> findBySubjectId(UUID subjectId);
    List<Notes> findBySubjectIdIn(List<UUID> subjectIds);
}
