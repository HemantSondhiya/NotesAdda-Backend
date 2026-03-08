package com.example.NotsHub.Repository;

import com.example.NotsHub.model.PendingUserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingUserRegistrationRepository extends JpaRepository<PendingUserRegistration, UUID> {

    Optional<PendingUserRegistration> findByEmail(String email);
    Optional<PendingUserRegistration> findByEmailIgnoreCase(String email);

    Optional<PendingUserRegistration> findByUsername(String username);
    Optional<PendingUserRegistration> findByUsernameIgnoreCase(String username);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
