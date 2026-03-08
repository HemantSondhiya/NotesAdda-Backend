package com.example.NotsHub.Repository;

import com.example.NotsHub.model.PasswordResetToken;
import com.example.NotsHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {


    void deleteByUser(User user);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByExpiresAtBefore(LocalDateTime time);

    Optional<PasswordResetToken> findByToken(String token);
}