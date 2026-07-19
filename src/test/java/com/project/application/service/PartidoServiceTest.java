package com.project.application.service;

import com.project.application.port.in.CreatePartidoUseCase.CreatePartidoCommand;
import com.project.application.port.in.RegistrarAsistenciaUseCase.AsistenciaItem;
import com.project.application.port.out.AsistenciaRepository;
import com.project.application.port.out.JugadorRepository;
import com.project.application.port.out.PartidoRepository;
import com.project.domain.exception.PartidoNotFoundException;
import com.project.domain.model.Asistencia;
import com.project.domain.model.Partido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartidoServiceTest {

    @Mock PartidoRepository   partidoRepository;
    @Mock AsistenciaRepository asistenciaRepository;
    @Mock JugadorRepository   jugadorRepository;
    @InjectMocks PartidoService partidoService;

    private Partido buildPartido() {
        return Partido.builder().id(1L).ligaId(2L).equipoId(3L)
                .rival("FC Rival").fecha(LocalDate.of(2024, 5, 10))
                .lugar("local").resultado("2-1").golesFavor(2).golesContra(1).build();
    }

    @Test
    void create_savesAndReturns() {
        when(partidoRepository.save(any())).thenReturn(buildPartido());
        Partido result = partidoService.create(new CreatePartidoCommand(
                2L, 3L, "FC Rival", LocalDate.of(2024, 5, 10), "local", "2-1", 2, 1));
        assertThat(result.getRival()).isEqualTo("FC Rival");
        verify(partidoRepository).save(any());
    }

    @Test
    void getByLigaId_returnsList() {
        when(partidoRepository.findByLigaId(2L)).thenReturn(List.of(buildPartido()));
        assertThat(partidoService.getByLigaId(2L)).hasSize(1);
    }

    @Test
    void createAll_guardaEnLoteYDevuelveLista() {
        when(partidoRepository.saveAll(any())).thenReturn(List.of(buildPartido(), buildPartido()));

        List<Partido> result = partidoService.createAll(List.of(
                new CreatePartidoCommand(2L, 3L, "Rival A", LocalDate.of(2024, 5, 10), null, "2-1", 2, 1),
                new CreatePartidoCommand(2L, 3L, "Rival B", LocalDate.of(2024, 5, 17), null, "0-0", 0, 0)));

        assertThat(result).hasSize(2);
        verify(partidoRepository).saveAll(any());
    }

    @Test
    void getByLigaId_paginado_devuelvePaginaYTotal() {
        when(partidoRepository.findByLigaId(2L, 0, 25)).thenReturn(List.of(buildPartido()));
        when(partidoRepository.countByLigaId(2L)).thenReturn(100L);

        var result = partidoService.getByLigaId(2L, 0, 25);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(100L);
    }

    @Test
    void getByEquipoId_returnsList() {
        when(partidoRepository.findByEquipoId(3L)).thenReturn(List.of(buildPartido()));
        assertThat(partidoService.getByEquipoId(3L)).hasSize(1);
    }

    @Test
    void getById_found_returnsPartido() {
        when(partidoRepository.findById(1L)).thenReturn(Optional.of(buildPartido()));
        assertThat(partidoService.getById(1L).getRival()).isEqualTo("FC Rival");
    }

    @Test
    void getById_notFound_throws() {
        when(partidoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> partidoService.getById(99L))
                .isInstanceOf(PartidoNotFoundException.class).hasMessageContaining("99");
    }

    @Test
    void delete_validPartido_deletesAsistenciasAndPartidoAndRecalculates() {
        when(partidoRepository.existsById(1L)).thenReturn(true);
        Asistencia previa = Asistencia.builder().partidoId(1L).jugadorId(10L).asistio(true).goles(1).build();
        when(asistenciaRepository.findByPartidoId(1L)).thenReturn(List.of(previa));

        partidoService.delete(1L);

        verify(asistenciaRepository).deleteByPartidoId(1L);
        verify(partidoRepository).deleteById(1L);
        verify(jugadorRepository).actualizarEstadisticas(List.of(10L));
    }

    @Test
    void delete_notFound_throws() {
        when(partidoRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> partidoService.delete(99L))
                .isInstanceOf(PartidoNotFoundException.class);
    }

    @Test
    void registrar_savesAsistenciasAndRecalculatesStats() {
        when(partidoRepository.existsById(1L)).thenReturn(true);
        Asistencia previa = Asistencia.builder().partidoId(1L).jugadorId(20L).asistio(true).goles(1).build();
        when(asistenciaRepository.findByPartidoId(1L)).thenReturn(List.of(previa));
        when(asistenciaRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        List<AsistenciaItem> items = List.of(new AsistenciaItem(10L, true, 2, 90, true));
        List<Asistencia> result = partidoService.registrar(1L, items);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getJugadorId()).isEqualTo(10L);
        verify(asistenciaRepository).deleteByPartidoId(1L);
        verify(jugadorRepository).actualizarEstadisticas(List.of(20L, 10L));
    }

    @Test
    void registrar_partidoNotFound_throws() {
        when(partidoRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> partidoService.registrar(99L, List.of()))
                .isInstanceOf(PartidoNotFoundException.class);
    }

    @Test
    void getByPartidoId_returnsList() {
        Asistencia a = Asistencia.builder().id(1L).partidoId(1L).jugadorId(10L)
                .asistio(true).goles(1).minutos(90).build();
        when(asistenciaRepository.findByPartidoId(1L)).thenReturn(List.of(a));
        assertThat(partidoService.getByPartidoId(1L)).hasSize(1);
    }
}
