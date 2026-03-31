package com.project.application.service;

import com.project.application.port.in.CreateEquipoUseCase.CreateEquipoCommand;
import com.project.application.port.in.UpdateEquipoUseCase.UpdateEquipoCommand;
import com.project.application.port.out.EquipoRepository;
import com.project.domain.exception.EquipoNotFoundException;
import com.project.domain.exception.UnauthorizedEquipoAccessException;
import com.project.domain.model.Equipo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipoServiceTest {

    @Mock EquipoRepository equipoRepository;
    @InjectMocks EquipoService equipoService;

    private Equipo buildEquipo() {
        return Equipo.builder().id(1L).nombre("FC Test").temporada("2024")
                .liga("Liga A").descripcion("Desc").userId(10L).build();
    }

    @Test
    void create_savesAndReturnsEquipo() {
        Equipo saved = buildEquipo();
        when(equipoRepository.save(any())).thenReturn(saved);

        Equipo result = equipoService.create(new CreateEquipoCommand("FC Test", "2024", "Liga A", "Desc", 10L));

        assertThat(result.getNombre()).isEqualTo("FC Test");
        assertThat(result.getId()).isEqualTo(1L);
        verify(equipoRepository).save(any());
    }

    @Test
    void getByUserId_returnsEquipos() {
        List<Equipo> equipos = List.of(buildEquipo());
        when(equipoRepository.findByUserId(10L)).thenReturn(equipos);

        List<Equipo> result = equipoService.getByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("FC Test");
    }

    @Test
    void getByUserId_empty_returnsEmptyList() {
        when(equipoRepository.findByUserId(99L)).thenReturn(List.of());

        List<Equipo> result = equipoService.getByUserId(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void update_validOwner_updatesAndReturns() {
        Equipo existing = buildEquipo();
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(equipoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Equipo result = equipoService.update(new UpdateEquipoCommand(1L, "FC Nuevo", "2025", "Liga B", "Nueva desc", 10L));

        assertThat(result.getNombre()).isEqualTo("FC Nuevo");
        assertThat(result.getTemporada()).isEqualTo("2025");
        assertThat(result.getLiga()).isEqualTo("Liga B");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        verify(equipoRepository).save(any());
    }

    @Test
    void update_preservesCreatedAtAndUserId() {
        Equipo existing = buildEquipo();
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(equipoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Equipo result = equipoService.update(new UpdateEquipoCommand(1L, "FC Nuevo", "2025", "Liga B", "Desc", 10L));

        assertThat(result.getCreatedAt()).isEqualTo(existing.getCreatedAt());
        assertThat(result.getUserId()).isEqualTo(existing.getUserId());
    }

    @Test
    void update_equipoNotFound_throwsEquipoNotFoundException() {
        when(equipoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipoService.update(
                new UpdateEquipoCommand(99L, "X", null, null, null, 10L)))
                .isInstanceOf(EquipoNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_differentUser_throwsUnauthorizedEquipoAccessException() {
        Equipo existing = buildEquipo(); // userId=10
        when(equipoRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> equipoService.update(
                new UpdateEquipoCommand(1L, "X", null, null, null, 99L)))
                .isInstanceOf(UnauthorizedEquipoAccessException.class);
    }
}
