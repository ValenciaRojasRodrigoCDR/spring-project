package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.port.in.CreateEquipoUseCase;
import com.project.application.port.in.GetEquiposQuery;
import com.project.application.port.in.GetJugadoresQuery;
import com.project.application.port.in.GetUserQuery;
import com.project.domain.model.Equipo;
import com.project.domain.model.Jugador;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.CreateEquipoRequest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EquipoControllerTest {

    @Mock CreateEquipoUseCase createEquipoUseCase;
    @Mock GetEquiposQuery getEquiposQuery;
    @Mock GetUserQuery getUserQuery;
    @Mock GetJugadoresQuery getJugadoresQuery;
    @InjectMocks EquipoController equipoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(equipoController).build();
    }

    private User buildUser() {
        return User.builder().id(1L).username("admin").password("pass")
                .role("ADMIN").nombre("Admin").apellidos("T").email("a@b.com").build();
    }

    private Equipo buildEquipo() {
        return Equipo.builder().id(1L).nombre("FC Test").temporada("2024")
                .liga("Liga A").descripcion("Desc").userId(1L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    private UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken("admin", null, List.of());
    }

    @Test
    void list_returnsEquipos() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(getEquiposQuery.getByUserId(1L)).thenReturn(List.of(buildEquipo()));

        mockMvc.perform(get("/api/equipos").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("FC Test"))
                .andExpect(jsonPath("$[0].liga").value("Liga A"));
    }

    @Test
    void list_empty_returnsEmptyArray() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(getEquiposQuery.getByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/equipos").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());
        when(createEquipoUseCase.create(any())).thenReturn(buildEquipo());

        mockMvc.perform(post("/api/equipos")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateEquipoRequest("FC Test", "2024", "Liga A", "Desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("FC Test"));
    }

    @Test
    void jugadores_returnsList() throws Exception {
        Jugador jugador = Jugador.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(5).partidosJugados(10)
                .golPorPartido(0.5).equipoId(1L).build();
        when(getJugadoresQuery.getByEquipoId(1L)).thenReturn(List.of(jugador));

        mockMvc.perform(get("/api/equipos/1/jugadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Leo"))
                .andExpect(jsonPath("$[0].dorsal").value(10));
    }
}
