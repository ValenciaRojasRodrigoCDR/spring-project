package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateJugadorUseCase;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JugadorControllerTest {

    @Mock CreateJugadorUseCase createJugadorUseCase;
    @Mock JugadorRepository jugadorRepository;
    @InjectMocks JugadorController jugadorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jugadorController).build();
    }

    private Jugador buildJugador(String fotoUrl) {
        return Jugador.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(0).partidosJugados(0)
                .golPorPartido(0).fotoUrl(fotoUrl).equipoId(5L).build();
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(createJugadorUseCase.create(any())).thenReturn(buildJugador(null));

        mockMvc.perform(multipart("/api/jugadores")
                        .param("nombre", "Leo")
                        .param("posicion", "DEL")
                        .param("dorsal", "10")
                        .param("edad", "25")
                        .param("equipoId", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Leo"))
                .andExpect(jsonPath("$.dorsal").value(10));
    }

    @Test
    void create_withFoto_returnsCreated() throws Exception {
        when(createJugadorUseCase.create(any())).thenReturn(buildJugador("jugadores/img.jpg"));

        MockMultipartFile foto = new MockMultipartFile("foto", "img.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/jugadores")
                        .file(foto)
                        .param("nombre", "Leo")
                        .param("equipoId", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fotoUrl").value("jugadores/img.jpg"));
    }

    @Test
    void getFoto_jugadorNotFound_returns404() throws Exception {
        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/jugadores/99/foto"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFoto_jugadorWithNoFotoUrl_returns404() throws Exception {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(buildJugador(null)));

        mockMvc.perform(get("/api/jugadores/1/foto"))
                .andExpect(status().isNotFound());
    }
}
