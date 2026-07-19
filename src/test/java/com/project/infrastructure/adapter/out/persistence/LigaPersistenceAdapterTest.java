package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.Liga;
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
class LigaPersistenceAdapterTest {

    @Mock LigaJpaRepository ligaJpaRepository;
    @Mock LigaEquipoJpaRepository ligaEquipoJpaRepository;
    @InjectMocks LigaPersistenceAdapter adapter;

    private LigaEntity buildEntity() {
        return LigaEntity.builder().id(1L).nombre("Liga Test").temporada("2024-25")
                .descripcion("Desc").createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).userId(10L).build();
    }

    private Liga buildDomain() {
        return Liga.builder().id(1L).nombre("Liga Test").temporada("2024-25")
                .descripcion("Desc").userId(10L).build();
    }

    @Test
    void save_persistsAndReturnsMappedDomain() {
        LigaEntity saved = buildEntity();
        when(ligaJpaRepository.save(any())).thenReturn(saved);

        Liga result = adapter.save(buildDomain());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Liga Test");
        assertThat(result.getTemporada()).isEqualTo("2024-25");
        assertThat(result.getUserId()).isEqualTo(10L);
    }

    @Test
    void findById_found_returnsMappedOptional() {
        when(ligaJpaRepository.findById(1L)).thenReturn(Optional.of(buildEntity()));

        Optional<Liga> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Liga Test");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(ligaJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findByUserId_returnsMappedList() {
        when(ligaJpaRepository.findByUserId(10L)).thenReturn(List.of(buildEntity(), buildEntity()));

        List<Liga> result = adapter.findByUserId(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void deleteById_delegatesToRepository() {
        adapter.deleteById(1L);
        verify(ligaJpaRepository).deleteById(1L);
    }

    @Test
    void addEquipo_whenNotExists_savesRelation() {
        LigaEquipoId id = new LigaEquipoId(1L, 2L);
        when(ligaEquipoJpaRepository.existsById(id)).thenReturn(false);

        adapter.addEquipo(1L, 2L);

        verify(ligaEquipoJpaRepository).save(new LigaEquipoEntity(id));
    }

    @Test
    void addEquipo_whenAlreadyExists_doesNotSave() {
        when(ligaEquipoJpaRepository.existsById(any())).thenReturn(true);

        adapter.addEquipo(1L, 2L);

        verify(ligaEquipoJpaRepository, never()).save(any());
    }

    @Test
    void removeEquipo_delegatesToRepository() {
        adapter.removeEquipo(1L, 2L);
        verify(ligaEquipoJpaRepository).deleteById(new LigaEquipoId(1L, 2L));
    }

    @Test
    void findEquipoIdsByLigaId_returnsIds() {
        when(ligaEquipoJpaRepository.findEquipoIdsByLigaId(1L)).thenReturn(List.of(2L, 3L));

        List<Long> result = adapter.findEquipoIdsByLigaId(1L);

        assertThat(result).containsExactly(2L, 3L);
    }
}
