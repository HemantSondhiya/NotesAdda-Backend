package com.example.NotsHub.Repository;

import com.example.NotsHub.model.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
    boolean existsByCode(String code);
    boolean existsBySlug(String slug);
    Optional<University> findBySlug(String slug);
    Page<University> findByIsActiveTrue(Pageable pageable);

    @Query("""
SELECT DISTINCT u
FROM University u
LEFT JOIN FETCH u.programs p
LEFT JOIN FETCH p.branches b
WHERE u.isActive = true
""")
    List<University> findAllWithPrograms();
}
