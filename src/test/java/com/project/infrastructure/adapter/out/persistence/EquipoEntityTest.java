package com.project.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EquipoEntityTest {

    @Test
    void prePersist_setsCreatedAt_whenNull() {
        EquipoEntity entity = new EquipoEntity();
        assertThat(entity.getCreatedAt()).isNull();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_doesNotOverwrite_whenAlreadySet() {
        EquipoEntity entity = EquipoEntity.builder()
                .nombre("FC Test")
                .userId(1L)
                .build();
        entity.prePersist();
        var first = entity.getCreatedAt();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isEqualTo(first);
    }

    @Test
    void builder_setsAllFields() {
        EquipoEntity entity = EquipoEntity.builder()
                .id(1L)
                .nombre("FC Test")
                .temporada("2024/25")
                .liga("Primera")
                .descripcion("Desc")
                .userId(99L)
                .build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNombre()).isEqualTo("FC Test");
        assertThat(entity.getTemporada()).isEqualTo("2024/25");
        assertThat(entity.getLiga()).isEqualTo("Primera");
        assertThat(entity.getDescripcion()).isEqualTo("Desc");
        assertThat(entity.getUserId()).isEqualTo(99L);
    }
}
