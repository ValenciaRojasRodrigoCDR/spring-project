package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.port.in.AddEquipoToLigaUseCase;
import com.project.application.port.in.CreateLigaUseCase;
import com.project.application.port.in.DeleteLigaUseCase;
import com.project.application.port.in.GetLigasQuery;
import com.project.application.port.in.GetUserQuery;
import com.project.application.port.in.UpdateLigaUseCase;
import com.project.domain.exception.LigaNotFoundException;
import com.project.domain.exception.UnauthorizedLigaAccessException;
import com.project.domain.model.Liga;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.CreateLigaRequest;
import com.project.infrastructure.adapter.in.web.dto.UpdateLigaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LigaControllerTest {

    @Mock CreateLigaUseCase     createLigaUseCase;
    @Mock UpdateLigaUseCase     updateLigaUseCase;
    @Mock DeleteLigaUseCase     deleteLigaUseCase;
    @Mock GetLigasQuery         getLigasQuery;
    @Mock AddEquipoToLigaUseCase addEquipoToLigaUseCase;
    @Mock GetUserQuery          getUserQuery;
    @InjectMocks LigaController ligaController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ligaController)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private User buildUser() {
        return User.builder().id(1L).username("admin").password("pass")
                .role("ADMIN").nombre("Admin").apellidos("T").email("a@b.com").build();
    }

    private Liga buildLiga() {
        return Liga.builder().id(1L).nombre("Liga A").temporada("2024")
                .descripcion("Desc").createdAt(LocalDateTime.of(2024,1,1,0,0)).userId(1L).build();
    }

    private UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken("admin", null, List.of());
    }

    @Test
    void list_returnsLigas() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(getLigasQuery.getByUserId(1L)).thenReturn(List.of(buildLiga()));
        when(addEquipoToLigaUseCase.getEquipoIds(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/ligas").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Liga A"));
    }

    @Test
    void list_empty_returnsEmptyArray() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(getLigasQuery.getByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/ligas").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void get_returnsLiga() throws Exception {
        when(getLigasQuery.getById(1L)).thenReturn(buildLiga());
        when(addEquipoToLigaUseCase.getEquipoIds(1L)).thenReturn(List.of(5L));

        mockMvc.perform(get("/api/ligas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Liga A"))
                .andExpect(jsonPath("$.equipoIds[0]").value(5));
    }

    @Test
    void get_notFound_returns404() throws Exception {
        when(getLigasQuery.getById(99L)).thenThrow(new LigaNotFoundException(99L));

        mockMvc.perform(get("/api/ligas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(createLigaUseCase.create(any())).thenReturn(buildLiga());

        mockMvc.perform(post("/api/ligas").principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLigaRequest("Liga A", "2024", "Desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Liga A"));
    }

    @Test
    void create_missingNombre_returns400() throws Exception {
        mockMvc.perform(post("/api/ligas").principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateLigaRequest("", null, null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validRequest_returnsOk() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(updateLigaUseCase.update(any())).thenReturn(buildLiga());
        when(addEquipoToLigaUseCase.getEquipoIds(1L)).thenReturn(List.of());

        mockMvc.perform(put("/api/ligas/1").principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLigaRequest("Liga A", "2024", "Desc"))))
                .andExpect(status().isOk());
    }

    @Test
    void update_notOwner_returns403() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(updateLigaUseCase.update(any())).thenThrow(new UnauthorizedLigaAccessException());

        mockMvc.perform(put("/api/ligas/1").principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateLigaRequest("Liga A", "2024", "Desc"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_validOwner_returns204() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        doNothing().when(deleteLigaUseCase).delete(1L, 1L);

        mockMvc.perform(delete("/api/ligas/1").principal(mockAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        doThrow(new LigaNotFoundException(1L)).when(deleteLigaUseCase).delete(1L, 1L);

        mockMvc.perform(delete("/api/ligas/1").principal(mockAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addEquipo_returns204() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        doNothing().when(addEquipoToLigaUseCase).addEquipo(1L, 5L, 1L);

        mockMvc.perform(post("/api/ligas/1/equipos/5").principal(mockAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeEquipo_returns204() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        doNothing().when(addEquipoToLigaUseCase).removeEquipo(1L, 5L, 1L);

        mockMvc.perform(delete("/api/ligas/1/equipos/5").principal(mockAuth()))
                .andExpect(status().isNoContent());
    }

    @Test
    void equipos_returnsList() throws Exception {
        when(addEquipoToLigaUseCase.getEquipoIds(1L)).thenReturn(List.of(5L, 6L));

        mockMvc.perform(get("/api/ligas/1/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(5))
                .andExpect(jsonPath("$[1]").value(6));
    }
}
