
package com.example.NotsHub.service;

import com.example.NotsHub.Repository.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PasswordResetTokenCleanupService {

    private final PasswordResetTokenRepository repository;

    public PasswordResetTokenCleanupService(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void cleanupExpiredResetTokens() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}