package com.project.application.service;

import com.project.application.port.in.CreateJugadorUseCase;
import com.project.application.port.in.UpdateJugadorUseCase;
import com.project.application.port.out.FileStoragePort;
import com.project.application.port.out.JugadorRepository;
import com.project.domain.model.Jugador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JugadorService implements CreateJugadorUseCase, UpdateJugadorUseCase {

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
                .fotoConvertida(false)
                .equipoId(command.equipoId())
                .build());
    }

    @Override
    public Jugador update(UpdateJugadorCommand command) {
        Jugador existing = jugadorRepository.findById(command.id())
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado"));

        String fotoUrl = existing.getFotoUrl();
        if (command.foto() != null && !command.foto().isEmpty()) {
            fotoUrl = fileStoragePort.store(command.foto(), "jugadores");
        }

        boolean fotoConvertida = (fotoUrl != null && fotoUrl.equals(existing.getFotoUrl()))
                ? existing.isFotoConvertida()
                : false;

        return jugadorRepository.save(Jugador.builder()
                .id(existing.getId())
                .nombre(command.nombre())
                .posicion(command.posicion())
                .dorsal(command.dorsal())
                .edad(command.edad())
                .totalGoals(existing.getTotalGoals())
                .partidosJugados(existing.getPartidosJugados())
                .golPorPartido(existing.getGolPorPartido())
                .fotoUrl(fotoUrl)
                .fotoConvertida(fotoConvertida)
                .equipoId(existing.getEquipoId())
                .build());
    }
}
