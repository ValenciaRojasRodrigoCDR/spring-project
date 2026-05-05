package com.project.application.service;

import com.project.application.port.in.CreateUserUseCase;
import com.project.application.port.out.UserRepository;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserManagementService service;

    private User buildUser(Long id, String role) {
        return User.builder().id(id).username("user1").password("hashed")
                .role(role).nombre("Test").apellidos("User").email("t@t.com").build();
    }

    @Test
    void create_encodesPasswordAndSaves() {
        when(passwordEncoder.encode("rawpass")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var cmd = new CreateUserUseCase.CreateUserCommand(
                "user1", "rawpass", "ROLE_USER", "Test", "User", "t@t.com", null);
        User result = service.create(cmd);

        assertThat(result.getPassword()).isEqualTo("hashed");
        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
        verify(passwordEncoder).encode("rawpass");
        verify(userRepository).save(any());
    }

    @Test
    void create_withJugadorId_persists() {
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var cmd = new CreateUserUseCase.CreateUserCommand(
                "jugador1", "pass", "ROLE_JUGADOR", "Juan", "Gómez", "j@j.com", 42L);
        User result = service.create(cmd);

        assertThat(result.getJugadorId()).isEqualTo(42L);
    }

    @Test
    void getAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(buildUser(1L, "ROLE_ADMIN"), buildUser(2L, "ROLE_USER")));

        List<User> result = service.getAll();

        assertThat(result).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void updateRole_existingUser_updatesRoleAndJugadorId() {
        User existing = buildUser(1L, "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateRole(1L, "ROLE_ADMIN", 99L);

        assertThat(result.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(result.getJugadorId()).isEqualTo(99L);
        assertThat(result.getUsername()).isEqualTo("user1");
    }

    @Test
    void updateRole_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateRole(99L, "ROLE_ADMIN", null))
                .isInstanceOf(UserNotFoundException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRole_preservesExistingFields() {
        User existing = buildUser(1L, "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = service.updateRole(1L, "ROLE_LIGA_OWNER", null);

        assertThat(result.getUsername()).isEqualTo(existing.getUsername());
        assertThat(result.getPassword()).isEqualTo(existing.getPassword());
        assertThat(result.getNombre()).isEqualTo(existing.getNombre());
        assertThat(result.getEmail()).isEqualTo(existing.getEmail());
    }
}
