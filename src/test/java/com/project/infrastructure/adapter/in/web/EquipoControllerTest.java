package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.port.in.CreateEquipoUseCase;
import com.project.application.port.in.GetEquiposQuery;
import com.project.application.port.in.GetEstadisticasAvanzadasQuery;
import com.project.application.port.in.GetEstadisticasAvanzadasQuery.Result;
import com.project.application.port.in.GetJugadoresQuery;
import com.project.application.port.in.PageResult;
import com.project.application.port.in.UpdateEquipoUseCase;
import com.project.domain.exception.EquipoNotFoundException;
import com.project.domain.exception.UnauthorizedEquipoAccessException;
import com.project.domain.model.Equipo;
import com.project.domain.model.Jugador;
import com.project.infrastructure.adapter.in.web.dto.CreateEquipoRequest;
import com.project.infrastructure.adapter.in.web.dto.UpdateEquipoRequest;
import com.project.infrastructure.config.AuthDetails;
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

    @Mock CreateEquipoUseCase           createEquipoUseCase;
    @Mock UpdateEquipoUseCase           updateEquipoUseCase;
    @Mock GetEquiposQuery               getEquiposQuery;
    @Mock GetJugadoresQuery             getJugadoresQuery;
    @Mock GetEstadisticasAvanzadasQuery getEstadisticasAvanzadasQuery;
    @InjectMocks EquipoController equipoController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(equipoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Equipo buildEquipo() {
        return Equipo.builder().id(1L).nombre("FC Test").temporada("2024")
                .liga("Liga A").descripcion("Desc").userId(1L)
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    private UsernamePasswordAuthenticationToken mockAuth() {
        var auth = new UsernamePasswordAuthenticationToken("admin", null, List.of());
        auth.setDetails(new AuthDetails(1L, null));
        return auth;
    }

    @Test
    void list_returnsEquipos() throws Exception {
        when(getEquiposQuery.getByUserId(1L)).thenReturn(List.of(buildEquipo()));

        mockMvc.perform(get("/api/equipos").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("FC Test"))
                .andExpect(jsonPath("$[0].liga").value("Liga A"));
    }

    @Test
    void list_empty_returnsEmptyArray() throws Exception {
        when(getEquiposQuery.getByUserId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/equipos").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void create_returnsCreated() throws Exception {
        when(createEquipoUseCase.create(any())).thenReturn(buildEquipo());

        mockMvc.perform(post("/api/equipos")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateEquipoRequest("FC Test", "2024", "Liga A", "Desc"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("FC Test"));
    }

    @Test
    void update_validRequest_returnsOk() throws Exception {
        Equipo updated = Equipo.builder().id(1L).nombre("FC Nuevo").temporada("2025")
                .liga("Liga B").descripcion("Desc").userId(1L).createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
        when(updateEquipoUseCase.update(any())).thenReturn(updated);

        mockMvc.perform(put("/api/equipos/1")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEquipoRequest("FC Nuevo", "2025", "Liga B", "Desc"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("FC Nuevo"));
    }

    @Test
    void update_equipoNotFound_returns404() throws Exception {
        when(updateEquipoUseCase.update(any())).thenThrow(new EquipoNotFoundException(1L));

        mockMvc.perform(put("/api/equipos/1")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEquipoRequest("FC Nuevo", "2025", "Liga B", "Desc"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void update_notOwner_returns403() throws Exception {
        when(updateEquipoUseCase.update(any())).thenThrow(new UnauthorizedEquipoAccessException());

        mockMvc.perform(put("/api/equipos/1")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEquipoRequest("FC Nuevo", "2025", "Liga B", "Desc"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void update_missingNombre_returns400() throws Exception {
        mockMvc.perform(put("/api/equipos/1")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateEquipoRequest("", "2025", null, null))))
                .andExpect(status().isBadRequest());
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

    @Test
    void jugadores_conPage_devuelvePaginaConTotales() throws Exception {
        Jugador jugador = Jugador.builder().id(1L).nombre("Leo").posicion("DEL")
                .dorsal(10).edad(25).totalGoals(5).partidosJugados(10)
                .golPorPartido(0.5).equipoId(1L).build();
        when(getJugadoresQuery.getByEquipoId(1L, 0, 25))
                .thenReturn(new PageResult<>(List.of(jugador), 500L));

        mockMvc.perform(get("/api/equipos/1/jugadores?page=0&size=25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Leo"))
                .andExpect(jsonPath("$.totalElements").value(500))
                .andExpect(jsonPath("$.totalPages").value(20))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void estadisticasAvanzadas_returnsOkWithMappedData() throws Exception {
        GetEstadisticasAvanzadasQuery.PosicionStat posicion =
                new GetEstadisticasAvanzadasQuery.PosicionStat("DEL", 2, 8, 80.0);
        GetEstadisticasAvanzadasQuery.JugadorStat eficiente =
                new GetEstadisticasAvanzadasQuery.JugadorStat("Leo", "2.00 goles/partido");

        Result result = new Result(
                25.0, 4.0, 7.5, 1,
                List.of(posicion),
                eficiente,
                new GetEstadisticasAvanzadasQuery.JugadorStat("Marco", "5 partidos"),
                new GetEstadisticasAvanzadasQuery.JugadorStat("Javi",  "19 años"),
                new GetEstadisticasAvanzadasQuery.JugadorStat("Paco",  "35 años"),
                new GetEstadisticasAvanzadasQuery.JugadorStat("Javi",  "Dorsal 1"),
                new GetEstadisticasAvanzadasQuery.JugadorStat("Paco",  "Dorsal 99")
        );
        when(getEstadisticasAvanzadasQuery.get(1L)).thenReturn(result);

        mockMvc.perform(get("/api/equipos/1/estadisticas-avanzadas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedioEdad").value(25.0))
                .andExpect(jsonPath("$.promedioGoles").value(4.0))
                .andExpect(jsonPath("$.promedioPartidos").value(7.5))
                .andExpect(jsonPath("$.jugadoresSinGoles").value(1))
                .andExpect(jsonPath("$.porPosicion[0].posicion").value("DEL"))
                .andExpect(jsonPath("$.porPosicion[0].porcentajeGoles").value(80.0))
                .andExpect(jsonPath("$.masEficiente.nombre").value("Leo"))
                .andExpect(jsonPath("$.masEficiente.valor").value("2.00 goles/partido"))
                .andExpect(jsonPath("$.masJoven.nombre").value("Javi"))
                .andExpect(jsonPath("$.masVeterano.nombre").value("Paco"))
                .andExpect(jsonPath("$.dorsalMasBajo.valor").value("Dorsal 1"))
                .andExpect(jsonPath("$.dorsalMasAlto.valor").value("Dorsal 99"));
    }
}
