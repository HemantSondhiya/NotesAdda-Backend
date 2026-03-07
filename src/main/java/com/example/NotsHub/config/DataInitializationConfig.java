package com.example.NotsHub.config;

import com.example.NotsHub.Repository.RoleRepository;
import com.example.NotsHub.Repository.UniversityRepository;
import com.example.NotsHub.Repository.ProgramRepository;
import com.example.NotsHub.Repository.BranchRepository;
import com.example.NotsHub.Repository.SubjectRepository;
import com.example.NotsHub.Repository.NotesRepository;
import com.example.NotsHub.model.AppRole;
import com.example.NotsHub.model.Role;
import com.example.NotsHub.util.SlugUtil;
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

    @Bean
    public CommandLineRunner backfillSlugs(
            UniversityRepository universityRepository,
            ProgramRepository programRepository,
            BranchRepository branchRepository,
            SubjectRepository subjectRepository,
            NotesRepository notesRepository) {
        return args -> {
            logger.info("Checking for missing slugs to backfill...");
            
            universityRepository.findAll().stream()
                    .filter(u -> u.getSlug() == null || u.getSlug().isBlank())
                    .forEach(u -> {
                        u.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(u.getName()), universityRepository::existsBySlug));
                        universityRepository.save(u);
                    });

            programRepository.findAll().stream()
                    .filter(p -> p.getSlug() == null || p.getSlug().isBlank())
                    .forEach(p -> {
                        p.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(p.getName()), programRepository::existsBySlug));
                        programRepository.save(p);
                    });

            branchRepository.findAll().stream()
                    .filter(b -> b.getSlug() == null || b.getSlug().isBlank())
                    .forEach(b -> {
                        b.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(b.getName()), branchRepository::existsBySlug));
                        branchRepository.save(b);
                    });

            subjectRepository.findAll().stream()
                    .filter(s -> s.getSlug() == null || s.getSlug().isBlank())
                    .forEach(s -> {
                        s.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(s.getName()), subjectRepository::existsBySlug));
                        subjectRepository.save(s);
                    });

            notesRepository.findAll().stream()
                    .filter(n -> n.getSlug() == null || n.getSlug().isBlank())
                    .forEach(n -> {
                        n.setSlug(SlugUtil.makeUnique(SlugUtil.generateSlug(n.getTitle()), notesRepository::existsBySlug));
                        notesRepository.save(n);
                    });

            logger.info("Slug backfill completed.");
        };
    }
}
