package com.project.application.service;

import com.project.application.port.in.LoginUseCase.LoginCommand;
import com.project.application.port.out.UserRepository;
import com.project.domain.exception.InvalidCredentialsException;
import com.project.domain.model.User;
import com.project.infrastructure.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock JwtUtil jwtUtil;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks AuthService authService;

    private User buildUser() {
        return User.builder()
                .id(1L).username("admin").password("hashed")
                .role("ADMIN").nombre("Admin").apellidos("Test").email("a@b.com")
                .build();
    }

    @Test
    void login_success_returnsToken() {
        User user = buildUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMIN")).thenReturn("jwt-token");

        String token = authService.login(new LoginCommand("admin", "plain"));

        assertThat(token).isEqualTo("jwt-token");
        verify(jwtUtil).generateToken("admin", "ADMIN");
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("unknown", "pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        User user = buildUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("admin", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtUtil, never()).generateToken(any(), any());
    }
}
