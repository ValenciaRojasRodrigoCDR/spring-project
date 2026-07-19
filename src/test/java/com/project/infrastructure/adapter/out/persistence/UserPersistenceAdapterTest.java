package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock UserJpaRepository jpaRepository;
    @InjectMocks UserPersistenceAdapter adapter;

    private UserEntity buildEntity(Long id, String username) {
        return UserEntity.builder().id(id).username(username).password("hashed")
                .role("ROLE_ADMIN").nombre("Admin").apellidos("Test").email(username + "@b.com")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).jugadorId(null).build();
    }

    @Test
    void findByUsername_found_returnsMappedUser() {
        when(jpaRepository.findByUsername("admin")).thenReturn(Optional.of(buildEntity(1L, "admin")));

        Optional<User> result = adapter.findByUsername("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
        assertThat(result.get().getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(result.get().getEmail()).isEqualTo("admin@b.com");
        assertThat(result.get().getPassword()).isEqualTo("hashed");
    }

    @Test
    void findByUsername_notFound_returnsEmpty() {
        when(jpaRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThat(adapter.findByUsername("ghost")).isEmpty();
    }

    @Test
    void findById_found_returnsMappedUser() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildEntity(1L, "admin")));

        Optional<User> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findAll_returnsMappedList() {
        when(jpaRepository.findAll()).thenReturn(List.of(buildEntity(1L, "u1"), buildEntity(2L, "u2")));

        List<User> result = adapter.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("u1");
        assertThat(result.get(1).getUsername()).isEqualTo("u2");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(jpaRepository.findAll()).thenReturn(List.of());

        assertThat(adapter.findAll()).isEmpty();
    }

    @Test
    void save_persistsAndReturnsMappedUser() {
        UserEntity saved = buildEntity(5L, "newuser");
        when(jpaRepository.save(any())).thenReturn(saved);

        User user = User.builder().username("newuser").password("hashed").role("ROLE_USER")
                .nombre("New").apellidos("User").email("newuser@b.com").build();
        User result = adapter.save(user);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(jpaRepository).save(any());
    }

    @Test
    void save_withJugadorId_mapsField() {
        UserEntity saved = UserEntity.builder().id(3L).username("jugador").password("p")
                .role("ROLE_JUGADOR").nombre("J").apellidos("G").email("j@j.com")
                .createdAt(LocalDateTime.now()).jugadorId(42L).build();
        when(jpaRepository.save(any())).thenReturn(saved);

        User user = User.builder().username("jugador").password("p").role("ROLE_JUGADOR")
                .nombre("J").apellidos("G").email("j@j.com").jugadorId(42L).build();
        User result = adapter.save(user);

        assertThat(result.getJugadorId()).isEqualTo(42L);
    }
}
