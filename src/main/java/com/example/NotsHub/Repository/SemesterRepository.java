package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Semester;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    @Override
    @EntityGraph(attributePaths = {"branch", "branch.program", "branch.program.university"})
    Page<Semester> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"branch", "branch.program", "branch.program.university"})
    Optional<Semester> findById(UUID id);

    boolean existsByNumberAndBranchId(Short number, UUID branchId);
    List<Semester> findByBranchId(UUID branchId);
    long countByBranchId(UUID branchId);
    Page<Semester> findByBranchId(UUID branchId, Pageable pageable);
    List<Semester> findByBranchIdIn(List<UUID> branchIds);

    boolean existsByNumberAndBranchIdAndIdNot(@NotNull(message = "Semester number is required") Short number, @NotNull(message = "Branch ID is required") UUID branchId, UUID id);
}
