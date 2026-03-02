package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    boolean existsByCodeAndSemesterId(String code, UUID semesterId);
    List<Subject> findBySemesterId(UUID semesterId);
    List<Subject> findBySemesterIdIn(List<UUID> semesterIds);
}
