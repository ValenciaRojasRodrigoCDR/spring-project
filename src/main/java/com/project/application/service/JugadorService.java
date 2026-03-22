package com.project.application.service;

import com.project.application.port.in.CreateJugadorUseCase;
import com.project.application.port.out.FileStoragePort;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JugadorService implements CreateJugadorUseCase {

    private final JugadorRepository jugadorRepository;
    private final FileStoragePort fileStoragePort;

    @Override
    public Jugador create(CreateJugadorCommand command) {
        String fotoUrl = null;
        if (command.foto() != null && !command.foto().isEmpty()) {
            fotoUrl = fileStoragePort.store(command.foto(), "jugadores");
        }

        return jugadorRepository.save(Jugador.builder()
                .nombre(command.nombre())
                .posicion(command.posicion())
                .dorsal(command.dorsal())
                .edad(command.edad())
                .totalGoals(0)
                .partidosJugados(0)
                .golPorPartido(0)
                .fotoUrl(fotoUrl)
                .equipoId(command.equipoId())
                .build());
    }
}
