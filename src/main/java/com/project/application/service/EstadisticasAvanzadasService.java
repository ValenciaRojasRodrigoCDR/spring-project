package com.project.application.service;

import com.project.application.port.in.GetEstadisticasAvanzadasQuery;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticasAvanzadasService implements GetEstadisticasAvanzadasQuery {

    private static final int MIN_PARTIDOS_EFICIENCIA = 3;

    private final JugadorRepository jugadorRepository;

    @Override
    public Result get(Long equipoId) {
        List<Jugador> jugadores = jugadorRepository.findByEquipoId(equipoId);
        if (jugadores.isEmpty()) return emptyResult();

        double promedioEdad = jugadores.stream()
                .filter(j -> j.getEdad() != null)
                .mapToInt(Jugador::getEdad)
                .average().orElse(0);

        double promedioGoles = jugadores.stream()
                .mapToInt(Jugador::getTotalGoals).average().orElse(0);

        double promedioPartidos = jugadores.stream()
                .mapToInt(Jugador::getPartidosJugados).average().orElse(0);

        int jugadoresSinGoles = (int) jugadores.stream()
                .filter(j -> j.getTotalGoals() == 0).count();

        int totalGoles = jugadores.stream().mapToInt(Jugador::getTotalGoals).sum();

        Map<String, List<Jugador>> grupoPosicion = jugadores.stream()
                .filter(j -> j.getPosicion() != null && !j.getPosicion().isBlank())
                .collect(Collectors.groupingBy(Jugador::getPosicion));

        List<PosicionStat> porPosicion = grupoPosicion.entrySet().stream()
                .map(e -> {
                    int goles = e.getValue().stream().mapToInt(Jugador::getTotalGoals).sum();
                    double pct = totalGoles > 0 ? Math.round((goles * 1000.0 / totalGoles)) / 10.0 : 0;
                    return new PosicionStat(e.getKey(), e.getValue().size(), goles, pct);
                })
                .sorted(Comparator.comparing(PosicionStat::posicion))
                .toList();

        JugadorStat masEficiente = jugadores.stream()
                .filter(j -> j.getPartidosJugados() >= MIN_PARTIDOS_EFICIENCIA)
                .max(Comparator.comparingDouble(Jugador::getGolPorPartido))
                .map(j -> new JugadorStat(j.getNombre(), String.format("%.2f goles/partido", j.getGolPorPartido())))
                .orElse(new JugadorStat("—", "—"));

        JugadorStat masPartidosSinMarcar = jugadores.stream()
                .max(Comparator.comparingInt(j -> j.getPartidosJugados() - j.getTotalGoals()))
                .map(j -> new JugadorStat(j.getNombre(),
                        (j.getPartidosJugados() - j.getTotalGoals()) + " partidos"))
                .orElse(new JugadorStat("—", "—"));

        JugadorStat masJoven = jugadores.stream()
                .filter(j -> j.getEdad() != null)
                .min(Comparator.comparingInt(Jugador::getEdad))
                .map(j -> new JugadorStat(j.getNombre(), j.getEdad() + " años"))
                .orElse(new JugadorStat("—", "—"));

        JugadorStat masVeterano = jugadores.stream()
                .filter(j -> j.getEdad() != null)
                .max(Comparator.comparingInt(Jugador::getEdad))
                .map(j -> new JugadorStat(j.getNombre(), j.getEdad() + " años"))
                .orElse(new JugadorStat("—", "—"));

        JugadorStat dorsalMasBajo = jugadores.stream()
                .filter(j -> j.getDorsal() != null)
                .min(Comparator.comparingInt(Jugador::getDorsal))
                .map(j -> new JugadorStat(j.getNombre(), "Dorsal " + j.getDorsal()))
                .orElse(new JugadorStat("—", "—"));

        JugadorStat dorsalMasAlto = jugadores.stream()
                .filter(j -> j.getDorsal() != null)
                .max(Comparator.comparingInt(Jugador::getDorsal))
                .map(j -> new JugadorStat(j.getNombre(), "Dorsal " + j.getDorsal()))
                .orElse(new JugadorStat("—", "—"));

        return new Result(
                Math.round(promedioEdad * 10.0) / 10.0,
                Math.round(promedioGoles * 10.0) / 10.0,
                Math.round(promedioPartidos * 10.0) / 10.0,
                jugadoresSinGoles,
                porPosicion,
                masEficiente,
                masPartidosSinMarcar,
                masJoven,
                masVeterano,
                dorsalMasBajo,
                dorsalMasAlto
        );
    }

    private Result emptyResult() {
        return new Result(0, 0, 0, 0, List.of(),
                new JugadorStat("—", "—"), new JugadorStat("—", "—"),
                new JugadorStat("—", "—"), new JugadorStat("—", "—"),
                new JugadorStat("—", "—"), new JugadorStat("—", "—"));
    }
}
