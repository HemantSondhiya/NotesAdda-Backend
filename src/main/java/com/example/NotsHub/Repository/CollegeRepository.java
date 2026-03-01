package com.example.NotsHub.Repository;

import com.example.NotsHub.model.College;
import com.example.NotsHub.payload.CollegeDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface CollegeRepository extends JpaRepository<College, UUID> {
    boolean existsByCode(String upperCase);

    Collection<Object> findAllByIsActiveTrue();
}
