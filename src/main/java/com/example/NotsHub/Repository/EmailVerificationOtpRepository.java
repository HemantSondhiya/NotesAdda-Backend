package com.example.NotsHub.Repository;

import com.example.NotsHub.model.EmailVerificationOtp;
import com.example.NotsHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, UUID> {
    Optional<EmailVerificationOtp> findByUser(User user);
}
