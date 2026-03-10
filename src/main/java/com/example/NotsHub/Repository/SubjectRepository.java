package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    boolean existsByCodeAndSemesterId(String code, UUID semesterId);
    boolean existsBySlug(String slug);
    Optional<Subject> findBySlug(String slug);
    List<Subject> findBySemesterId(UUID semesterId);
    long countBySemesterId(UUID semesterId);
    Page<Subject> findBySemesterId(UUID semesterId, Pageable pageable);
    List<Subject> findBySemesterIdIn(List<UUID> semesterIds);
}
