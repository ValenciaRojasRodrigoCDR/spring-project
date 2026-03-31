package com.project.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void prePersist_setsCreatedAt_whenNull() {
        UserEntity entity = new UserEntity();
        assertThat(entity.getCreatedAt()).isNull();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_doesNotOverwrite_whenAlreadySet() {
        UserEntity entity = UserEntity.builder()
                .username("admin")
                .password("hash")
                .build();
        entity.prePersist();
        var first = entity.getCreatedAt();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isEqualTo(first);
    }

    @Test
    void builder_setsAllFields() {
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .username("admin")
                .password("hash")
                .role("ADMIN")
                .nombre("Admin")
                .apellidos("Test")
                .email("admin@test.com")
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUsername()).isEqualTo("admin");
        assertThat(entity.getPassword()).isEqualTo("hash");
        assertThat(entity.getRole()).isEqualTo("ADMIN");
        assertThat(entity.getNombre()).isEqualTo("Admin");
        assertThat(entity.getApellidos()).isEqualTo("Test");
        assertThat(entity.getEmail()).isEqualTo("admin@test.com");
    }
}
