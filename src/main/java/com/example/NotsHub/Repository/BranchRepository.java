package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    // Find all branches by program ID
    List<Branch> findByProgramId(UUID programId);
    boolean existsBySlug(String slug);
    Optional<Branch> findBySlug(String slug);
    Page<Branch> findByProgramId(UUID programId, Pageable pageable);
    List<Branch> findByProgramIdIn(List<UUID> programIds);
    // Find branch by name and program
    Branch findByNameAndProgramId(String name, UUID programId);
    boolean existsByNameAndProgramId(String name, UUID programId);

    // Find all branches by program with semesters eagerly loaded
    @Query("SELECT DISTINCT b FROM Branch b LEFT JOIN FETCH b.semesters WHERE b.program.id = :programId")
List<Branch> findByProgramIdWithSemesters(@Param("programId") UUID programId);
}

