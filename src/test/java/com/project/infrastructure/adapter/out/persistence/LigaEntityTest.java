package com.project.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LigaEntityTest {

    @Test
    void builder_setsAllFields() {
        LocalDateTime now = LocalDateTime.of(2024, 5, 1, 10, 0);
        LigaEntity entity = LigaEntity.builder()
                .id(1L).nombre("Liga A").temporada("2024-25")
                .descripcion("Desc").createdAt(now).userId(99L).build();

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getNombre()).isEqualTo("Liga A");
        assertThat(entity.getTemporada()).isEqualTo("2024-25");
        assertThat(entity.getDescripcion()).isEqualTo("Desc");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUserId()).isEqualTo(99L);
    }

    @Test
    void prePersist_setsCreatedAtWhenNull() {
        LigaEntity entity = LigaEntity.builder().nombre("Liga B").userId(1L).build();
        assertThat(entity.getCreatedAt()).isNull();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void prePersist_doesNotOverwriteExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2023, 1, 1, 0, 0);
        LigaEntity entity = LigaEntity.builder().nombre("Liga C").userId(1L).createdAt(fixed).build();

        entity.prePersist();

        assertThat(entity.getCreatedAt()).isEqualTo(fixed);
    }

    @Test
    void noArgsConstructor_createsEmptyEntity() {
        LigaEntity entity = new LigaEntity();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getNombre()).isNull();
    }
}
