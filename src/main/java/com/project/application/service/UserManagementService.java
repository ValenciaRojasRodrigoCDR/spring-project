package com.project.application.service;

import com.project.application.port.in.CreateUserUseCase;
import com.project.application.port.out.UserRepository;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService implements CreateUserUseCase {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User create(CreateUserCommand command) {
        return userRepository.save(User.builder()
                .username(command.username())
                .password(passwordEncoder.encode(command.password()))
                .role(command.role())
                .nombre(command.nombre())
                .apellidos(command.apellidos())
                .email(command.email())
                .jugadorId(command.jugadorId())
                .build());
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User updateRole(Long id, String newRole, Long jugadorId) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
        return userRepository.save(User.builder()
                .id(existing.getId())
                .username(existing.getUsername())
                .password(existing.getPassword())
                .role(newRole)
                .nombre(existing.getNombre())
                .apellidos(existing.getApellidos())
                .email(existing.getEmail())
                .createdAt(existing.getCreatedAt())
                .jugadorId(jugadorId)
                .build());
    }
}
