package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.Partido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartidoPersistenceAdapterTest {

    @Mock PartidoJpaRepository jpaRepository;
    @InjectMocks PartidoPersistenceAdapter adapter;

    private PartidoEntity buildEntity() {
        return PartidoEntity.builder().id(1L).ligaId(5L).equipoId(2L).rival("Real Madrid")
                .fecha(LocalDate.of(2024, 3, 10)).lugar("local").resultado("2-1")
                .golesFavor(2).golesContra(1).createdAt(LocalDateTime.of(2024, 3, 10, 20, 0)).build();
    }

    private Partido buildDomain() {
        return Partido.builder().id(1L).ligaId(5L).equipoId(2L).rival("Real Madrid")
                .fecha(LocalDate.of(2024, 3, 10)).lugar("local").resultado("2-1")
                .golesFavor(2).golesContra(1).build();
    }

    @Test
    void save_persistsAndReturnsMappedDomain() {
        when(jpaRepository.save(any())).thenReturn(buildEntity());

        Partido result = adapter.save(buildDomain());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRival()).isEqualTo("Real Madrid");
        assertThat(result.getGolesFavor()).isEqualTo(2);
        assertThat(result.getLigaId()).isEqualTo(5L);
    }

    @Test
    void findById_found_returnsMappedOptional() {
        when(jpaRepository.findById(1L)).thenReturn(Optional.of(buildEntity()));

        Optional<Partido> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getRival()).isEqualTo("Real Madrid");
        assertThat(result.get().getResultado()).isEqualTo("2-1");
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(99L)).isEmpty();
    }

    @Test
    void findByLigaId_returnsMappedList() {
        when(jpaRepository.findByLigaId(5L)).thenReturn(List.of(buildEntity(), buildEntity()));

        List<Partido> result = adapter.findByLigaId(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLigaId()).isEqualTo(5L);
    }

    @Test
    void findByEquipoId_returnsMappedList() {
        when(jpaRepository.findByEquipoId(2L)).thenReturn(List.of(buildEntity()));

        List<Partido> result = adapter.findByEquipoId(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEquipoId()).isEqualTo(2L);
    }

    @Test
    void saveAll_persisteLoteYDevuelveMapeados() {
        when(jpaRepository.saveAll(any())).thenReturn(List.of(buildEntity(), buildEntity()));

        List<Partido> result = adapter.saveAll(List.of(buildDomain(), buildDomain()));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRival()).isEqualTo("Real Madrid");
        verify(jpaRepository).saveAll(any());
    }

    @Test
    void findByLigaId_paginado_delegaConPageRequest() {
        when(jpaRepository.findByLigaId(eq(5L), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(List.of(buildEntity()));

        List<Partido> result = adapter.findByLigaId(5L, 0, 25);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLigaId()).isEqualTo(5L);
    }

    @Test
    void countByLigaId_delegaAlRepositorio() {
        when(jpaRepository.countByLigaId(5L)).thenReturn(100L);

        assertThat(adapter.countByLigaId(5L)).isEqualTo(100L);
    }

    @Test
    void deleteById_delegatesToRepository() {
        adapter.deleteById(1L);
        verify(jpaRepository).deleteById(1L);
    }

    @Test
    void save_mapsAllFieldsCorrectly() {
        when(jpaRepository.save(any())).thenReturn(buildEntity());

        Partido result = adapter.save(buildDomain());

        assertThat(result.getFecha()).isEqualTo(LocalDate.of(2024, 3, 10));
        assertThat(result.getLugar()).isEqualTo("local");
        assertThat(result.getGolesContra()).isEqualTo(1);
        assertThat(result.getEquipoId()).isEqualTo(2L);
    }
}
