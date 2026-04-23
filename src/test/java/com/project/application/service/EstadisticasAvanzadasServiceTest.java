package com.project.application.service;

import com.project.application.port.in.GetEstadisticasAvanzadasQuery.Result;
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
class EstadisticasAvanzadasServiceTest {

    @Mock  JugadorRepository jugadorRepository;
    @InjectMocks EstadisticasAvanzadasService service;

    private Jugador jugador(Long id, String nombre, String posicion,
                            Integer dorsal, Integer edad, int goles, int partidos) {
        double ratio = partidos > 0 ? (double) goles / partidos : 0;
        return Jugador.builder()
                .id(id).nombre(nombre).posicion(posicion).dorsal(dorsal).edad(edad)
                .totalGoals(goles).partidosJugados(partidos).golPorPartido(ratio)
                .equipoId(1L).build();
    }

    // ── empty team ────────────────────────────────────────────────────────────

    @Test
    void get_emptyTeam_returnsAllDefaults() {
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(List.of());

        Result r = service.get(1L);

        assertThat(r.promedioEdad()).isZero();
        assertThat(r.promedioGoles()).isZero();
        assertThat(r.promedioPartidos()).isZero();
        assertThat(r.jugadoresSinGoles()).isZero();
        assertThat(r.porPosicion()).isEmpty();
        assertThat(r.masEficiente().nombre()).isEqualTo("—");
        assertThat(r.masPartidosSinMarcar().nombre()).isEqualTo("—");
        assertThat(r.masJoven().nombre()).isEqualTo("—");
        assertThat(r.masVeterano().nombre()).isEqualTo("—");
        assertThat(r.dorsalMasBajo().nombre()).isEqualTo("—");
        assertThat(r.dorsalMasAlto().nombre()).isEqualTo("—");
    }

    // ── full team, all data present ───────────────────────────────────────────

    @Test
    void get_fullTeam_computesAllStats() {
        // Alpha: DEL, dorsal 9, edad 22, 10 goles, 5 partidos → ratio 2.0
        // Beta:  MED, dorsal 7, edad 28,  3 goles, 10 partidos → ratio 0.3
        // Gamma: DEF, dorsal 4, edad 25,  0 goles,  8 partidos → ratio 0.0
        List<Jugador> equipo = List.of(
                jugador(1L, "Alpha", "DEL", 9, 22, 10, 5),
                jugador(2L, "Beta",  "MED", 7, 28,  3, 10),
                jugador(3L, "Gamma", "DEF", 4, 25,  0,  8)
        );
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(equipo);

        Result r = service.get(1L);

        assertThat(r.promedioEdad()).isEqualTo(25.0);
        assertThat(r.promedioGoles()).isEqualTo(4.3);
        assertThat(r.promedioPartidos()).isEqualTo(7.7);
        assertThat(r.jugadoresSinGoles()).isEqualTo(1);

        assertThat(r.masEficiente().nombre()).isEqualTo("Alpha");
        assertThat(r.masEficiente().valor()).contains("2.00");

        assertThat(r.masPartidosSinMarcar().nombre()).isEqualTo("Gamma"); // 8-0=8
        assertThat(r.masPartidosSinMarcar().valor()).contains("8");

        assertThat(r.masJoven().nombre()).isEqualTo("Alpha");
        assertThat(r.masJoven().valor()).contains("22");

        assertThat(r.masVeterano().nombre()).isEqualTo("Beta");
        assertThat(r.masVeterano().valor()).contains("28");

        assertThat(r.dorsalMasBajo().nombre()).isEqualTo("Gamma");
        assertThat(r.dorsalMasBajo().valor()).contains("4");

        assertThat(r.dorsalMasAlto().nombre()).isEqualTo("Alpha");
        assertThat(r.dorsalMasAlto().valor()).contains("9");

        assertThat(r.porPosicion()).hasSize(3);
        // sorted by posicion: DEF, DEL, MED
        assertThat(r.porPosicion().get(0).posicion()).isEqualTo("DEF");
        assertThat(r.porPosicion().get(1).posicion()).isEqualTo("DEL");
        assertThat(r.porPosicion().get(1).porcentajeGoles()).isEqualTo(76.9); // 10/13*100
        assertThat(r.porPosicion().get(2).porcentajeGoles()).isEqualTo(23.1); // 3/13*100
    }

    // ── null edad and dorsal → default JugadorStat ────────────────────────────

    @Test
    void get_nullEdadNullDorsal_defaultsForAgeAndDorsal() {
        List<Jugador> equipo = List.of(
                jugador(1L, "Alpha", "DEL", null, null, 5, 5)
        );
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(equipo);

        Result r = service.get(1L);

        assertThat(r.promedioEdad()).isZero();
        assertThat(r.masJoven().nombre()).isEqualTo("—");
        assertThat(r.masVeterano().nombre()).isEqualTo("—");
        assertThat(r.dorsalMasBajo().nombre()).isEqualTo("—");
        assertThat(r.dorsalMasAlto().nombre()).isEqualTo("—");
    }

    // ── no jugador meets min-partidos threshold → masEficiente defaults ───────

    @Test
    void get_noJugadorMeetsMinPartidos_masEficienteIsDefault() {
        // MIN_PARTIDOS_EFICIENCIA = 3; jugadores with 0, 1, 2 partidos
        List<Jugador> equipo = List.of(
                jugador(1L, "A", "DEL", 9, 20, 3, 0),
                jugador(2L, "B", "MED", 8, 22, 2, 1),
                jugador(3L, "C", "DEF", 5, 24, 1, 2)
        );
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(equipo);

        Result r = service.get(1L);

        assertThat(r.masEficiente().nombre()).isEqualTo("—");
        assertThat(r.masEficiente().valor()).isEqualTo("—");
    }

    // ── all goles = 0 → porcentajeGoles is 0 for all positions ───────────────

    @Test
    void get_allGolesZero_porcentajeGolesIsCero() {
        List<Jugador> equipo = List.of(
                jugador(1L, "Alpha", "DEL", 9, 22, 0, 5),
                jugador(2L, "Beta",  "MED", 7, 25, 0, 8)
        );
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(equipo);

        Result r = service.get(1L);

        assertThat(r.porPosicion()).isNotEmpty().allSatisfy(p ->
                assertThat(p.porcentajeGoles()).isZero());
    }

    // ── null and blank posicion → filtered out of porPosicion ────────────────

    @Test
    void get_nullAndBlankPosicion_porPosicionIsEmpty() {
        List<Jugador> equipo = List.of(
                jugador(1L, "Alpha", null,  9, 22, 5, 5),
                jugador(2L, "Beta",  "   ", 7, 25, 3, 8)
        );
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(equipo);

        Result r = service.get(1L);

        assertThat(r.porPosicion()).isEmpty();
    }

    // ── single jugador covers both min/max branches in one pass ───────────────

    @Test
    void get_singleJugador_samePlaysAllRoles() {
        List<Jugador> equipo = List.of(
                jugador(1L, "Solo", "POR", 1, 30, 0, 10)
        );
        when(jugadorRepository.findByEquipoId(1L)).thenReturn(equipo);

        Result r = service.get(1L);

        assertThat(r.masJoven().nombre()).isEqualTo("Solo");
        assertThat(r.masVeterano().nombre()).isEqualTo("Solo");
        assertThat(r.dorsalMasBajo().nombre()).isEqualTo("Solo");
        assertThat(r.dorsalMasAlto().nombre()).isEqualTo("Solo");
        assertThat(r.masEficiente().nombre()).isEqualTo("Solo");
        assertThat(r.jugadoresSinGoles()).isEqualTo(1);
        assertThat(r.porPosicion()).hasSize(1);
        assertThat(r.porPosicion().get(0).porcentajeGoles()).isZero();
    }
}
