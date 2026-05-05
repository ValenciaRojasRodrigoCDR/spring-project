package com.project.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PartidoEntityTest {

    @Test
    void builder_setsAllFields() {
        LocalDate fecha = LocalDate.of(2024, 3, 15);
        LocalDateTime createdAt = LocalDateTime.of(2024, 3, 15, 20, 0);
        PartidoEntity entity = PartidoEntity.builder()
                .id(1L).ligaId(5L).equipoId(2L).rival("Barcelona")
                .fecha(fecha).lugar("local").resultado("3-0")
                .golesFavor(3).golesContra(0).createdAt(createdAt).build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getLigaId()).isEqualTo(5L);
        assertThat(entity.getEquipoId()).isEqualTo(2L);
        assertThat(entity.getRival()).isEqualTo("Barcelona");
        assertThat(entity.getFecha()).isEqualTo(fecha);
        assertThat(entity.getLugar()).isEqualTo("local");
        assertThat(entity.getResultado()).isEqualTo("3-0");
        assertThat(entity.getGolesFavor()).isEqualTo(3);
        assertThat(entity.getGolesContra()).isEqualTo(0);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void prePersist_setsCreatedAtWhenNull() {
        PartidoEntity entity = PartidoEntity.builder().equipoId(1L).rival("Rival").build();
        assertThat(entity.getCreatedAt()).isNull();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void prePersist_doesNotOverwriteExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2023, 6, 1, 12, 0);
        PartidoEntity entity = PartidoEntity.builder().equipoId(1L).rival("R").createdAt(fixed).build();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isEqualTo(fixed);
    }

    @Test
    void noArgsConstructor_createsEmptyEntity() {
        PartidoEntity entity = new PartidoEntity();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getRival()).isNull();
        assertThat(entity.getGolesFavor()).isZero();
    }
}
