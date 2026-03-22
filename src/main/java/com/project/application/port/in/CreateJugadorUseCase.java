package com.project.application.port.in;

import com.project.domain.model.Jugador;
import org.springframework.web.multipart.MultipartFile;

public interface CreateJugadorUseCase {

    record CreateJugadorCommand(
            String nombre,
            String posicion,
            Integer dorsal,
            Integer edad,
            Long equipoId,
            MultipartFile foto
    ) {}

    Jugador create(CreateJugadorCommand command);
}
