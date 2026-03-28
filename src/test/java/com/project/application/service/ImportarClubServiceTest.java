package com.project.application.service;

import com.project.application.port.in.ImportarClubUseCase.ImportarClubCommand;
import com.project.application.port.in.ImportarClubUseCase.ImportarClubResult;
import com.project.application.port.in.ImportarClubUseCase.JugadorData;
import com.project.application.port.out.EquipoRepository;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Equipo;
import com.project.domain.model.Jugador;
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
class ImportarClubServiceTest {

    @Mock EquipoRepository equipoRepository;
    @Mock JugadorRepository jugadorRepository;
    @InjectMocks ImportarClubService importarClubService;

    @Test
    void importar_createsEquipoAndJugadores() {
        Equipo equipo = Equipo.builder().id(1L).nombre("FC").temporada("2024")
                .liga("L1").descripcion("D").userId(10L).build();
        Jugador jugador = Jugador.builder().id(1L).nombre("Leo")
                .totalGoals(5).partidosJugados(10).golPorPartido(0.5).equipoId(1L).build();

        when(equipoRepository.save(any())).thenReturn(equipo);
        when(jugadorRepository.save(any())).thenReturn(jugador);

        List<JugadorData> jugadoresData = List.of(new JugadorData("Leo", 5, 10, 0.5));
        ImportarClubCommand command = new ImportarClubCommand("FC", "2024", "L1", "D", 10L, jugadoresData);

        ImportarClubResult result = importarClubService.importar(command);

        assertThat(result.equipo().getNombre()).isEqualTo("FC");
        assertThat(result.jugadores()).hasSize(1);
        assertThat(result.jugadores().get(0).getNombre()).isEqualTo("Leo");
        verify(equipoRepository).save(any());
        verify(jugadorRepository, times(1)).save(any());
    }

    @Test
    void importar_emptyJugadores_createsOnlyEquipo() {
        Equipo equipo = Equipo.builder().id(2L).nombre("FC2").temporada("2025")
                .liga("L2").descripcion("").userId(1L).build();
        when(equipoRepository.save(any())).thenReturn(equipo);

        ImportarClubCommand command = new ImportarClubCommand("FC2", "2025", "L2", "", 1L, List.of());

        ImportarClubResult result = importarClubService.importar(command);

        assertThat(result.equipo().getNombre()).isEqualTo("FC2");
        assertThat(result.jugadores()).isEmpty();
        verify(jugadorRepository, never()).save(any());
    }
}
