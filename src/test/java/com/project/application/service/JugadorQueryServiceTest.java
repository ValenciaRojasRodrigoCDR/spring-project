package com.project.application.service;

import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JugadorQueryServiceTest {

    @Mock JugadorRepository jugadorRepository;
    @InjectMocks JugadorQueryService jugadorQueryService;

    @Test
    void getByEquipoId_returnsJugadores() {
        Jugador jugador = Jugador.builder().id(1L).nombre("Leo").equipoId(3L)
                .totalGoals(0).partidosJugados(0).golPorPartido(0).build();
        when(jugadorRepository.findByEquipoId(3L)).thenReturn(List.of(jugador));

        List<Jugador> result = jugadorQueryService.getByEquipoId(3L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Leo");
    }

    @Test
    void getByEquipoId_empty_returnsEmptyList() {
        when(jugadorRepository.findByEquipoId(99L)).thenReturn(List.of());

        List<Jugador> result = jugadorQueryService.getByEquipoId(99L);

        assertThat(result).isEmpty();
    }
}
