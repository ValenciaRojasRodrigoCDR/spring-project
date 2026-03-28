package com.project.application.service;

import com.project.application.port.out.UserRepository;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserQueryService userQueryService;

    private User buildUser() {
        return User.builder().id(1L).username("admin").password("pass")
                .role("ADMIN").nombre("Admin").apellidos("Test").email("a@b.com").build();
    }

    @Test
    void getByUsername_found_returnsUser() {
        User user = buildUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        User result = userQueryService.getByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void getByUsername_notFound_throwsUserNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userQueryService.getByUsername("ghost"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("ghost");
    }
}
