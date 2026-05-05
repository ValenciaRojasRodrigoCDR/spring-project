package com.project.infrastructure.adapter.out.persistence;

import com.project.domain.model.Asistencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsistenciaPersistenceAdapterTest {

    @Mock AsistenciaJpaRepository jpaRepository;
    @InjectMocks AsistenciaPersistenceAdapter adapter;

    private AsistenciaEntity buildEntity() {
        return AsistenciaEntity.builder().id(1L).partidoId(10L).jugadorId(5L)
                .asistio(true).goles(2).minutos(90).titular(true).build();
    }

    private Asistencia buildDomain() {
        return Asistencia.builder().id(1L).partidoId(10L).jugadorId(5L)
                .asistio(true).goles(2).minutos(90).titular(true).build();
    }

    @Test
    void save_persistsAndReturnsMappedDomain() {
        when(jpaRepository.saveAndFlush(any())).thenReturn(buildEntity());

        Asistencia result = adapter.save(buildDomain());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getPartidoId()).isEqualTo(10L);
        assertThat(result.getJugadorId()).isEqualTo(5L);
        assertThat(result.isAsistio()).isTrue();
        assertThat(result.getGoles()).isEqualTo(2);
        assertThat(result.getMinutos()).isEqualTo(90);
        assertThat(result.isTitular()).isTrue();
    }

    @Test
    void findByPartidoId_returnsMappedList() {
        when(jpaRepository.findByPartidoId(10L)).thenReturn(List.of(buildEntity(), buildEntity()));

        List<Asistencia> result = adapter.findByPartidoId(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPartidoId()).isEqualTo(10L);
    }

    @Test
    void findByJugadorId_returnsMappedList() {
        when(jpaRepository.findByJugadorId(5L)).thenReturn(List.of(buildEntity()));

        List<Asistencia> result = adapter.findByJugadorId(5L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJugadorId()).isEqualTo(5L);
    }

    @Test
    void deleteByPartidoId_delegatesToRepository() {
        adapter.deleteByPartidoId(10L);
        verify(jpaRepository).deleteByPartidoId(10L);
    }

    @Test
    void save_withNotAsistioAndNotTitular_mapsCorrectly() {
        AsistenciaEntity entity = AsistenciaEntity.builder().id(2L).partidoId(10L).jugadorId(7L)
                .asistio(false).goles(0).minutos(0).titular(false).build();
        when(jpaRepository.saveAndFlush(any())).thenReturn(entity);

        Asistencia a = Asistencia.builder().partidoId(10L).jugadorId(7L)
                .asistio(false).goles(0).minutos(0).titular(false).build();
        Asistencia result = adapter.save(a);

        assertThat(result.isAsistio()).isFalse();
        assertThat(result.isTitular()).isFalse();
        assertThat(result.getGoles()).isZero();
    }
}
