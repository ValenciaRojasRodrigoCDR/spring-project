package com.project.application.service;

import com.project.application.port.in.CreateJugadorUseCase.CreateJugadorCommand;
import com.project.application.port.in.UpdateJugadorUseCase.UpdateJugadorCommand;
import com.project.application.port.out.FileStoragePort;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JugadorServiceTest {

    @Mock JugadorRepository jugadorRepository;
    @Mock FileStoragePort fileStoragePort;
    @InjectMocks JugadorService jugadorService;

    private Jugador buildJugador(String fotoUrl) {
        return Jugador.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(0).partidosJugados(0)
                .golPorPartido(0).fotoUrl(fotoUrl).equipoId(5L).build();
    }

    @Test
    void create_withFoto_storesFileAndSaves() {
        MultipartFile foto = mock(MultipartFile.class);
        when(foto.isEmpty()).thenReturn(false);
        when(fileStoragePort.store(foto, "jugadores")).thenReturn("jugadores/img.jpg");
        when(jugadorRepository.save(any())).thenReturn(buildJugador("jugadores/img.jpg"));

        Jugador result = jugadorService.create(new CreateJugadorCommand("Leo", "DEL", 10, 25, 5L, foto));

        assertThat(result.getFotoUrl()).isEqualTo("jugadores/img.jpg");
        verify(fileStoragePort).store(foto, "jugadores");
        verify(jugadorRepository).save(any());
    }

    @Test
    void create_withoutFoto_savesWithNullUrl() {
        when(jugadorRepository.save(any())).thenReturn(buildJugador(null));

        Jugador result = jugadorService.create(new CreateJugadorCommand("Leo", "DEL", 10, 25, 5L, null));

        assertThat(result.getFotoUrl()).isNull();
        verify(fileStoragePort, never()).store(any(), any());
    }

    @Test
    void create_withEmptyFoto_savesWithNullUrl() {
        MultipartFile foto = mock(MultipartFile.class);
        when(foto.isEmpty()).thenReturn(true);
        when(jugadorRepository.save(any())).thenReturn(buildJugador(null));

        Jugador result = jugadorService.create(new CreateJugadorCommand("Leo", "DEL", 10, 25, 5L, foto));

        assertThat(result.getFotoUrl()).isNull();
        verify(fileStoragePort, never()).store(any(), eq("jugadores"));
    }

    @Test
    void update_withNewFoto_storesNewFile() {
        Jugador existing = buildJugador("jugadores/old.jpg");
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        MultipartFile foto = mock(MultipartFile.class);
        when(foto.isEmpty()).thenReturn(false);
        when(fileStoragePort.store(foto, "jugadores")).thenReturn("jugadores/new.jpg");
        when(jugadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Jugador result = jugadorService.update(new UpdateJugadorCommand(1L, "Leo", "DEL", 10, 25, foto));

        assertThat(result.getFotoUrl()).isEqualTo("jugadores/new.jpg");
        verify(fileStoragePort).store(foto, "jugadores");
    }

    @Test
    void update_withoutFoto_keepsExistingUrl() {
        Jugador existing = buildJugador("jugadores/old.jpg");
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jugadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Jugador result = jugadorService.update(new UpdateJugadorCommand(1L, "Leo", "DEL", 10, 25, null));

        assertThat(result.getFotoUrl()).isEqualTo("jugadores/old.jpg");
        verify(fileStoragePort, never()).store(any(), any());
    }

    @Test
    void update_withEmptyFoto_keepsExistingUrl() {
        Jugador existing = buildJugador("jugadores/old.jpg");
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        MultipartFile foto = mock(MultipartFile.class);
        when(foto.isEmpty()).thenReturn(true);
        when(jugadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Jugador result = jugadorService.update(new UpdateJugadorCommand(1L, "Leo", "DEL", 10, 25, foto));

        assertThat(result.getFotoUrl()).isEqualTo("jugadores/old.jpg");
        verify(fileStoragePort, never()).store(any(), any());
    }

    @Test
    void update_jugadorNotFound_throwsRuntimeException() {
        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jugadorService.update(
                new UpdateJugadorCommand(99L, "Leo", "DEL", 10, 25, null)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrado");
    }

    @Test
    void update_samePhotoUrl_preservesFotoConvertida() {
        Jugador existing = Jugador.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(5).partidosJugados(3)
                .golPorPartido(1).fotoUrl("jugadores/old.jpg").fotoConvertida(true).equipoId(5L).build();
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(jugadorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Jugador result = jugadorService.update(new UpdateJugadorCommand(1L, "Leo Updated", "DEL", 10, 25, null));

        assertThat(result.isFotoConvertida()).isTrue();
        assertThat(result.getTotalGoals()).isEqualTo(5);
        assertThat(result.getPartidosJugados()).isEqualTo(3);
    }
}
