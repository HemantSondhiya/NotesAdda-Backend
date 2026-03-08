package com.example.NotsHub.service;

import com.example.NotsHub.Repository.PendingUserRegistrationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PendingRegistrationCleanupService {

    private final PendingUserRegistrationRepository repository;

    public PendingRegistrationCleanupService(PendingUserRegistrationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void cleanupExpiredPendingUsers() {
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
