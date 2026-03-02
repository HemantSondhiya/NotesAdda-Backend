package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Semester;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    boolean existsByNumberAndBranchId(Short number, UUID branchId);
    List<Semester> findByBranchId(UUID branchId);
    List<Semester> findByBranchIdIn(List<UUID> branchIds);

    boolean existsByNumberAndBranchIdAndIdNot(@NotNull(message = "Semester number is required") Short number, @NotNull(message = "Branch ID is required") UUID branchId, UUID id);
}
