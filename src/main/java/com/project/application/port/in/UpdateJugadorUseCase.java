package com.project.application.port.in;

import com.project.domain.model.Jugador;
import org.springframework.web.multipart.MultipartFile;

public interface UpdateJugadorUseCase {

    record UpdateJugadorCommand(
            Long id,
            String nombre,
            String posicion,
            Integer dorsal,
            Integer edad,
            MultipartFile foto
    ) {}

    Jugador update(UpdateJugadorCommand command);
}
