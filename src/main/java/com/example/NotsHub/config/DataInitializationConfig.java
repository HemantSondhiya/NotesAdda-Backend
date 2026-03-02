package com.example.NotsHub.config;

import com.example.NotsHub.Repository.RoleRepository;
import com.example.NotsHub.model.AppRole;
import com.example.NotsHub.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializationConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationConfig.class);

    @Bean
    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {
            try {
                logger.info("Starting application role initialization...");

                initializeRole(roleRepository, AppRole.ROLE_STUDENT, "Default role for students");
                initializeRole(roleRepository, AppRole.ROLE_FACULTY, "Faculty role for uploading notes");
                initializeRole(roleRepository, AppRole.ROLE_UNIVERSITY_ADMIN, "University admin role for approving notes");
                initializeRole(roleRepository, AppRole.ROLE_SUPER_ADMIN, "Super admin role for onboarding universities");

                logger.info("All roles initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize roles during application startup", e);
                throw new RuntimeException("Critical: Role initialization failed - application cannot start", e);
            }
        };
    }

    private void initializeRole(RoleRepository roleRepository, AppRole roleEnum, String description) {
        try {
            roleRepository.findByRoleName(roleEnum)
                    .ifPresentOrElse(
                            role -> logger.debug("Role {} already exists in database", roleEnum),
                            () -> {
                                Role newRole = new Role(roleEnum);
                                roleRepository.save(newRole);
                                logger.info("Created new role: {} - {}", roleEnum, description);
                            }
                    );
        } catch (Exception e) {
            logger.error("Failed to initialize role: {} ({})", roleEnum, description, e);
            throw new RuntimeException("Failed to initialize role: " + roleEnum, e);
        }
    }
}
