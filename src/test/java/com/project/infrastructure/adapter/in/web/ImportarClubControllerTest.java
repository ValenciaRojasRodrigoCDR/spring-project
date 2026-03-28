package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.port.in.GetUserQuery;
import com.project.application.port.in.ImportarClubUseCase;
import com.project.application.port.in.ImportarClubUseCase.ImportarClubResult;
import com.project.domain.model.Equipo;
import com.project.domain.model.Jugador;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.ImportarClubRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImportarClubControllerTest {

    @Mock ImportarClubUseCase importarClubUseCase;
    @Mock GetUserQuery getUserQuery;
    @InjectMocks ImportarClubController importarClubController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(importarClubController).build();
    }

    private UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken("admin", null, List.of());
    }

    @Test
    void importar_returnsCreated() throws Exception {
        User user = User.builder().id(1L).username("admin").password("p")
                .role("ADMIN").nombre("A").apellidos("B").email("a@b.com").build();
        Equipo equipo = Equipo.builder().id(1L).nombre("FC").temporada("2024")
                .liga("L1").descripcion("D").userId(1L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
        Jugador jugador = Jugador.builder().id(1L).nombre("Leo")
                .totalGoals(5).partidosJugados(10).golPorPartido(0.5).equipoId(1L).build();

        when(getUserQuery.getByUsername("admin")).thenReturn(user);
        when(importarClubUseCase.importar(any())).thenReturn(new ImportarClubResult(equipo, List.of(jugador)));

        ImportarClubRequest request = new ImportarClubRequest("FC", "2024", "L1", "D",
                List.of(new ImportarClubRequest.JugadorDto("Leo", 5, 10, 0.5)));

        mockMvc.perform(post("/api/equipos/importar")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("FC"))
                .andExpect(jsonPath("$.jugadores[0].nombre").value("Leo"));
    }
}
