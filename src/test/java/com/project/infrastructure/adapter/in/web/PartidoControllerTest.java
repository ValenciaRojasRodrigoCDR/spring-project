package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.application.port.in.CreatePartidoUseCase;
import com.project.application.port.in.DeletePartidoUseCase;
import com.project.application.port.in.GetAsistenciasQuery;
import com.project.application.port.in.GetPartidosQuery;
import com.project.application.port.in.RegistrarAsistenciaUseCase;
import com.project.domain.exception.PartidoNotFoundException;
import com.project.domain.model.Asistencia;
import com.project.domain.model.Partido;
import com.project.infrastructure.adapter.in.web.dto.CreatePartidoRequest;
import com.project.infrastructure.adapter.in.web.dto.RegistrarAsistenciaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PartidoControllerTest {

    @Mock CreatePartidoUseCase       createPartidoUseCase;
    @Mock GetPartidosQuery           getPartidosQuery;
    @Mock DeletePartidoUseCase       deletePartidoUseCase;
    @Mock RegistrarAsistenciaUseCase registrarAsistenciaUseCase;
    @Mock GetAsistenciasQuery        getAsistenciasQuery;
    @InjectMocks PartidoController partidoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(partidoController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private Partido buildPartido() {
        return Partido.builder().id(1L).ligaId(2L).equipoId(3L)
                .rival("FC Rival").fecha(LocalDate.of(2024, 5, 10))
                .lugar("local").resultado("2-1").golesFavor(2).golesContra(1)
                .createdAt(LocalDateTime.of(2024, 5, 10, 12, 0)).build();
    }

    @Test
    void list_returnsPartidos() throws Exception {
        when(getPartidosQuery.getByLigaId(2L)).thenReturn(List.of(buildPartido()));

        mockMvc.perform(get("/api/ligas/2/partidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rival").value("FC Rival"))
                .andExpect(jsonPath("$[0].golesFavor").value(2));
    }

    @Test
    void list_empty_returnsEmptyArray() throws Exception {
        when(getPartidosQuery.getByLigaId(2L)).thenReturn(List.of());

        mockMvc.perform(get("/api/ligas/2/partidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void get_returnsPartido() throws Exception {
        when(getPartidosQuery.getById(1L)).thenReturn(buildPartido());

        mockMvc.perform(get("/api/ligas/2/partidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rival").value("FC Rival"));
    }

    @Test
    void get_notFound_returns404() throws Exception {
        when(getPartidosQuery.getById(99L)).thenThrow(new PartidoNotFoundException(99L));

        mockMvc.perform(get("/api/ligas/2/partidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(createPartidoUseCase.create(any())).thenReturn(buildPartido());
        CreatePartidoRequest req = new CreatePartidoRequest(3L, "FC Rival", LocalDate.of(2024,5,10), "local", null, 2, 1);

        mockMvc.perform(post("/api/ligas/2/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rival").value("FC Rival"));
    }

    @Test
    void create_missingRival_returns400() throws Exception {
        CreatePartidoRequest req = new CreatePartidoRequest(3L, "", null, null, null, 0, 0);

        mockMvc.perform(post("/api/ligas/2/partidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_returns204() throws Exception {
        doNothing().when(deletePartidoUseCase).delete(1L);

        mockMvc.perform(delete("/api/ligas/2/partidos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new PartidoNotFoundException(99L)).when(deletePartidoUseCase).delete(99L);

        mockMvc.perform(delete("/api/ligas/2/partidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarAsistencias_returnsOk() throws Exception {
        Asistencia a = Asistencia.builder().id(1L).partidoId(1L).jugadorId(10L)
                .asistio(true).goles(2).minutos(90).titular(true).build();
        when(registrarAsistenciaUseCase.registrar(eq(1L), any())).thenReturn(List.of(a));

        RegistrarAsistenciaRequest req = new RegistrarAsistenciaRequest(
                List.of(new RegistrarAsistenciaRequest.AsistenciaItem(10L, true, 2, 90, true)));

        mockMvc.perform(post("/api/ligas/2/partidos/1/asistencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jugadorId").value(10))
                .andExpect(jsonPath("$[0].goles").value(2));
    }

    @Test
    void getAsistencias_returnsOk() throws Exception {
        Asistencia a = Asistencia.builder().id(1L).partidoId(1L).jugadorId(10L)
                .asistio(true).goles(1).minutos(80).build();
        when(getAsistenciasQuery.getByPartidoId(1L)).thenReturn(List.of(a));

        mockMvc.perform(get("/api/ligas/2/partidos/1/asistencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jugadorId").value(10));
    }
}
