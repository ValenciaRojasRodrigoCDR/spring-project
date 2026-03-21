package com.project.application.port.in;

import com.project.domain.model.Equipo;
import com.project.domain.model.Jugador;

import java.util.List;

public interface ImportarClubUseCase {

    record JugadorData(String nombre, int totalGoals, int partidosJugados, double golPorPartido) {}

    record ImportarClubCommand(
            String nombre,
            String temporada,
            String liga,
            String descripcion,
            Long userId,
            List<JugadorData> jugadores
    ) {}

    record ImportarClubResult(Equipo equipo, List<Jugador> jugadores) {}

    ImportarClubResult importar(ImportarClubCommand command);
}
