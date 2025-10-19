package com.academia.backend.service;

import com.academia.backend.domain.UserEntity;
import com.academia.backend.repo.RoleRepo;
import com.academia.backend.repo.UserRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SuperAdminInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SuperAdminInitializer.class);

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final AuthService authService;

    @Value("${SUPER_ADMIN_EMAIL:superadmin@academy.com}")
    private String superAdminEmail;

    @Value("${SUPER_ADMIN_USERNAME:superadmin}")
    private String superAdminUsername;

    @Value("${SUPER_ADMIN_PASSWORD:SuperAdmin2025!}")
    private String superAdminPassword;

    @Value("${SUPER_ADMIN_FIRST_NAME:Super}")
    private String superAdminFirstName;

    @Value("${SUPER_ADMIN_LAST_NAME:Admin}")
    private String superAdminLastName;

    public SuperAdminInitializer(UserRepo userRepo, RoleRepo roleRepo, AuthService authService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.authService = authService;
    }

    @PostConstruct
    public void initializeSuperAdmin() {
        // Verificar si ya existe un superadmin
        if (userRepo.findByEmail(superAdminEmail).isPresent()) {
            logger.info("SuperAdmin already exists: {}", superAdminEmail);
            return;
        }

        // Crear el superadmin
        UserEntity superAdmin = new UserEntity();
        superAdmin.setEmail(superAdminEmail);
        superAdmin.setUsername(superAdminUsername);
        superAdmin.setPasswordHash(authService.hashPassword(superAdminPassword));
        superAdmin.setFirstName(superAdminFirstName);
        superAdmin.setLastName(superAdminLastName);

        // Assign SUPER_ADMIN role
        var superAdminRole = roleRepo.findByName("SUPER_ADMIN")
                .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));
        superAdmin.setRoleEntity(superAdminRole);

        userRepo.save(superAdmin);
        logger.info("SuperAdmin created successfully: {}", superAdminEmail);
    }
}