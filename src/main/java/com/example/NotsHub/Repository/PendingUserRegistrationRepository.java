package com.example.NotsHub.Repository;

import com.example.NotsHub.model.PendingUserRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingUserRegistrationRepository extends JpaRepository<PendingUserRegistration, Long> {

    Optional<PendingUserRegistration> findByEmail(String email);
    Optional<PendingUserRegistration> findByEmailIgnoreCase(String email);

    Optional<PendingUserRegistration> findByUsername(String username);
    Optional<PendingUserRegistration> findByUsernameIgnoreCase(String username);
}
