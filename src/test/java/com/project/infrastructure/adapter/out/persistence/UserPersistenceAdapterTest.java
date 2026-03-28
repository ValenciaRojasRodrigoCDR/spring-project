package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock UserJpaRepository jpaRepository;
    @InjectMocks UserPersistenceAdapter adapter;

    private UserEntity buildEntity() {
        return UserEntity.builder().id(1L).username("admin").password("hashed")
                .role("ADMIN").nombre("Admin").apellidos("Test").email("a@b.com")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    @Test
    void findByUsername_found_returnsMappedUser() {
        when(jpaRepository.findByUsername("admin")).thenReturn(Optional.of(buildEntity()));

        Optional<User> result = adapter.findByUsername("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
        assertThat(result.get().getRole()).isEqualTo("ADMIN");
        assertThat(result.get().getEmail()).isEqualTo("a@b.com");
        assertThat(result.get().getPassword()).isEqualTo("hashed");
    }

    @Test
    void findByUsername_notFound_returnsEmpty() {
        when(jpaRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        Optional<User> result = adapter.findByUsername("ghost");

        assertThat(result).isEmpty();
    }
}
