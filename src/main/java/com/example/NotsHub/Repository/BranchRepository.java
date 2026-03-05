package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    // Find all branches by program ID
    List<Branch> findByProgramId(UUID programId);
    Page<Branch> findByProgramId(UUID programId, Pageable pageable);
    List<Branch> findByProgramIdIn(List<UUID> programIds);

    // Check if branch exists by name and program
    boolean existsByNameAndProgramId(String name, UUID programId);

    // Find branch by name and program
    Branch findByNameAndProgramId(String name, UUID programId);

    // Find all branches by program with semesters eagerly loaded
    @Query("SELECT DISTINCT b FROM Branch b LEFT JOIN FETCH b.semesters WHERE b.program.id = :programId")
List<Branch> findByProgramIdWithSemesters(@Param("programId") UUID programId);
}

