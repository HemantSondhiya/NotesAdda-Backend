package com.example.NotsHub.Repository;

import com.example.NotsHub.model.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {

    List<Program> findByUniversityId(UUID universityId);
    Page<Program> findByUniversityId(UUID universityId, Pageable pageable);
    boolean existsBySlug(String slug);
    Optional<Program> findBySlug(String slug);

    @Query("SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.branches WHERE p.slug = :slug")
    Optional<Program> findBySlugWithBranches(@Param("slug") String slug);

    boolean existsByNameAndUniversityId(String name, UUID universityId);

    @Query("SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.branches WHERE p.name = :name AND p.university.id = :universityId")
    Program findByNameAndUniversityId(@Param("name") String name, @Param("universityId") UUID universityId);

    @Query("SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.branches WHERE p.university.id = :universityId")
    List<Program> findByUniversityIdWithBranches(@Param("universityId") UUID universityId);

    List<Program> findByUniversityIdIn(List<UUID> universityIds);

    @Query("SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.branches WHERE p.id = :id")
    Optional<Program> findByIdWithBranches(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.branches")
    List<Program> findAllWithBranches();

    @Query("""
SELECT DISTINCT p
FROM Program p
LEFT JOIN FETCH p.branches
WHERE p.id IN :ids
""")
    List<Program> findProgramsWithBranches(@Param("ids") List<UUID> ids);
}
