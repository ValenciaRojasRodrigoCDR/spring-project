package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateJugadorUseCase;
import com.project.application.port.in.UpdateJugadorUseCase;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JugadorControllerTest {

    @TempDir
    Path tempDir;

    @Mock CreateJugadorUseCase createJugadorUseCase;
    @Mock UpdateJugadorUseCase updateJugadorUseCase;
    @Mock JugadorRepository jugadorRepository;
    @InjectMocks JugadorController jugadorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jugadorController, "uploadDir", tempDir.toString());
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

    @Test
    void getById_found_returns200() throws Exception {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(buildJugador(null)));

        mockMvc.perform(get("/api/jugadores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Leo"))
                .andExpect(jsonPath("$.posicion").value("DEL"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(jugadorRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/jugadores/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_returnsOk() throws Exception {
        when(updateJugadorUseCase.update(any())).thenReturn(buildJugador(null));

        mockMvc.perform(multipart("/api/jugadores/1")
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("nombre", "Leo Updated")
                        .param("posicion", "DEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Leo"));
    }

    @Test
    void update_withFoto_returnsOk() throws Exception {
        when(updateJugadorUseCase.update(any())).thenReturn(buildJugador("jugadores/new.jpg"));
        MockMultipartFile foto = new MockMultipartFile("foto", "new.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/jugadores/1")
                        .file(foto)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .param("nombre", "Leo Updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotoUrl").value("jugadores/new.jpg"));
    }

    @Test
    void getFoto_fileNotOnDisk_returns404() throws Exception {
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(buildJugador("jugadores/missing.jpg")));

        mockMvc.perform(get("/api/jugadores/1/foto"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFoto_jpegFile_returnsImageJpeg() throws Exception {
        Path foto = tempDir.resolve("img.jpg");
        Files.write(foto, new byte[]{(byte)0xFF, (byte)0xD8});
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(buildJugador("img.jpg")));

        mockMvc.perform(get("/api/jugadores/1/foto"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"));
    }

    @Test
    void getFoto_pngFile_returnsImagePng() throws Exception {
        Path foto = tempDir.resolve("img.png");
        Files.write(foto, new byte[]{(byte)0x89, 0x50});
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(buildJugador("img.png")));

        mockMvc.perform(get("/api/jugadores/1/foto"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
    }

    @Test
    void getFoto_webpFile_returnsImageWebp() throws Exception {
        Path foto = tempDir.resolve("img.webp");
        Files.write(foto, new byte[]{0x52, 0x49});
        when(jugadorRepository.findById(1L)).thenReturn(Optional.of(buildJugador("img.webp")));

        mockMvc.perform(get("/api/jugadores/1/foto"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/webp"));
    }
}
