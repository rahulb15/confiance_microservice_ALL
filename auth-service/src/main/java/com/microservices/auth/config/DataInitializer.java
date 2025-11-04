package com.microservices.auth.config;

import com.microservices.auth.entity.Role;
import com.microservices.auth.entity.User;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeUsers();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            log.info("Initializing roles...");

            roleRepository.save(new Role("USER", "Default user role"));
            roleRepository.save(new Role("ADMIN", "Administrator role"));
            roleRepository.save(new Role("MODERATOR", "Moderator role"));

            log.info("Roles initialized successfully");
        }
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            log.info("Initializing default users...");

            // Create admin user
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            Role userRole = roleRepository.findByName("USER").orElseThrow();

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@microservices.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEnabled(true);
            admin.addRole(adminRole);
            admin.addRole(userRole);

            userRepository.save(admin);

            // Create test user
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("testuser@microservices.com");
            testUser.setPassword(passwordEncoder.encode("Test@123"));
            testUser.setEnabled(true);
            testUser.addRole(userRole);

            userRepository.save(testUser);

            log.info("Default users initialized successfully");
        }
    }
}
