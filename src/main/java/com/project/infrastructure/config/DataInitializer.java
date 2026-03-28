package com.project.infrastructure.config;

import com.project.infrastructure.adapter.out.persistence.UserEntity;
import com.project.infrastructure.adapter.out.persistence.UserJpaRepository;
import com.project.infrastructure.util.Constants;
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
        if (userJpaRepository.findByUsername(Constants.ADMIN_USERNAME).isEmpty()) {
            userJpaRepository.save(UserEntity.builder()
                    .username(Constants.ADMIN_USERNAME)
                    .password(passwordEncoder.encode(Constants.ADMIN_PASSWORD))
                    .role(Constants.ADMIN_ROLE)
                    .nombre(Constants.ADMIN_NOMBRE)
                    .apellidos(Constants.ADMIN_APELLIDOS)
                    .email(Constants.ADMIN_EMAIL)
                    .build());
        }
    }
}
