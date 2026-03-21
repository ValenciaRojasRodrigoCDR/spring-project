package com.project.application.service;

import com.project.application.port.in.ImportarClubUseCase;
import com.project.application.port.out.EquipoRepository;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Equipo;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportarClubService implements ImportarClubUseCase {

    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;

    @Override
    public ImportarClubResult importar(ImportarClubCommand command) {
        Equipo equipo = equipoRepository.save(Equipo.builder()
                .nombre(command.nombre())
                .temporada(command.temporada())
                .liga(command.liga())
                .descripcion(command.descripcion())
                .userId(command.userId())
                .build());

        List<Jugador> jugadores = command.jugadores().stream()
                .map(j -> jugadorRepository.save(Jugador.builder()
                        .nombre(j.nombre())
                        .totalGoals(j.totalGoals())
                        .partidosJugados(j.partidosJugados())
                        .golPorPartido(j.golPorPartido())
                        .equipoId(equipo.getId())
                        .build()))
                .toList();

        return new ImportarClubResult(equipo, jugadores);
    }
}
