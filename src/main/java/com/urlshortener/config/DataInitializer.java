package com.urlshortener.config;

import com.urlshortener.model.User;
import com.urlshortener.model.enums.Role;
import com.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createAdminUserIfNotExists();
    }

    private void createAdminUserIfNotExists() {
        String adminEmail = "admin@urlshortener.com";

        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists");
            return;
        }

        User admin = User.builder()
                .name("Admin")
                .email(adminEmail)
                .password(passwordEncoder.encode("admin123"))
                .roles(new HashSet<>(Set.of(Role.ADMIN, Role.USER)))
                .enabled(true)
                .emailVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        log.info("Admin user created successfully");
        log.info("Email: {}", adminEmail);
        log.info("Password: admin123");
    }
}
