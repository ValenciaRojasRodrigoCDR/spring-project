package com.project.application.service;

import com.project.application.port.in.CreateLigaUseCase.CreateLigaCommand;
import com.project.application.port.in.UpdateLigaUseCase.UpdateLigaCommand;
import com.project.application.port.out.LigaRepository;
import com.project.domain.exception.LigaNotFoundException;
import com.project.domain.exception.UnauthorizedLigaAccessException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LigaServiceTest {

    @Mock LigaRepository ligaRepository;
    @InjectMocks LigaService ligaService;

    private Liga buildLiga() {
        return Liga.builder().id(1L).nombre("Liga A").temporada("2024")
                .descripcion("Desc").createdAt(LocalDateTime.of(2024,1,1,0,0)).userId(10L).build();
    }

    @Test
    void create_savesAndReturns() {
        when(ligaRepository.save(any())).thenReturn(buildLiga());
        Liga result = ligaService.create(new CreateLigaCommand("Liga A", "2024", "Desc", 10L));
        assertThat(result.getNombre()).isEqualTo("Liga A");
        verify(ligaRepository).save(any());
    }

    @Test
    void getByUserId_returnsList() {
        when(ligaRepository.findByUserId(10L)).thenReturn(List.of(buildLiga()));
        assertThat(ligaService.getByUserId(10L)).hasSize(1);
    }

    @Test
    void getById_found_returnsLiga() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        assertThat(ligaService.getById(1L).getNombre()).isEqualTo("Liga A");
    }

    @Test
    void getById_notFound_throws() {
        when(ligaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ligaService.getById(99L))
                .isInstanceOf(LigaNotFoundException.class).hasMessageContaining("99");
    }

    @Test
    void update_validOwner_updates() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        when(ligaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Liga result = ligaService.update(new UpdateLigaCommand(1L, "Liga B", "2025", "Nueva desc", 10L));
        assertThat(result.getNombre()).isEqualTo("Liga B");
        assertThat(result.getUserId()).isEqualTo(10L);
    }

    @Test
    void update_preservesCreatedAt() {
        Liga existing = buildLiga();
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ligaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        Liga result = ligaService.update(new UpdateLigaCommand(1L, "Liga B", "2025", "Desc", 10L));
        assertThat(result.getCreatedAt()).isEqualTo(existing.getCreatedAt());
    }

    @Test
    void update_notFound_throws() {
        when(ligaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ligaService.update(new UpdateLigaCommand(99L, "X", null, null, 10L)))
                .isInstanceOf(LigaNotFoundException.class);
    }

    @Test
    void update_wrongUser_throwsUnauthorized() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        assertThatThrownBy(() -> ligaService.update(new UpdateLigaCommand(1L, "X", null, null, 99L)))
                .isInstanceOf(UnauthorizedLigaAccessException.class);
    }

    @Test
    void delete_validOwner_deletesLiga() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        ligaService.delete(1L, 10L);
        verify(ligaRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throws() {
        when(ligaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ligaService.delete(99L, 10L))
                .isInstanceOf(LigaNotFoundException.class);
    }

    @Test
    void delete_wrongUser_throwsUnauthorized() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        assertThatThrownBy(() -> ligaService.delete(1L, 99L))
                .isInstanceOf(UnauthorizedLigaAccessException.class);
        verify(ligaRepository, never()).deleteById(any());
    }

    @Test
    void addEquipo_validOwner_delegates() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        ligaService.addEquipo(1L, 5L, 10L);
        verify(ligaRepository).addEquipo(1L, 5L);
    }

    @Test
    void addEquipo_wrongUser_throwsUnauthorized() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        assertThatThrownBy(() -> ligaService.addEquipo(1L, 5L, 99L))
                .isInstanceOf(UnauthorizedLigaAccessException.class);
    }

    @Test
    void removeEquipo_validOwner_delegates() {
        when(ligaRepository.findById(1L)).thenReturn(Optional.of(buildLiga()));
        ligaService.removeEquipo(1L, 5L, 10L);
        verify(ligaRepository).removeEquipo(1L, 5L);
    }

    @Test
    void getEquipoIds_returnsList() {
        when(ligaRepository.findEquipoIdsByLigaId(1L)).thenReturn(List.of(5L, 6L));
        assertThat(ligaService.getEquipoIds(1L)).containsExactly(5L, 6L);
    }
}
