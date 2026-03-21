package com.project.infrastructure.config;

import com.project.infrastructure.adapter.out.persistence.UserEntity;
import com.project.infrastructure.adapter.out.persistence.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userJpaRepository.findByUsername("admin").isEmpty()) {
            userJpaRepository.save(UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin"))
                    .role("ADMIN")
                    .nombre("Admin")
                    .apellidos("4M Drink Team")
                    .email("admin@4mdrinkteam.com")
                    .build());
        }
    }
}
