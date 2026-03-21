package com.project.infrastructure.adapter.out.persistence;

import com.project.application.port.out.UserRepository;
import com.project.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    private User toDomain(UserEntity e) {
        return User.builder()
                .id(e.getId())
                .username(e.getUsername())
                .password(e.getPassword())
                .role(e.getRole())
                .nombre(e.getNombre())
                .apellidos(e.getApellidos())
                .email(e.getEmail())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
