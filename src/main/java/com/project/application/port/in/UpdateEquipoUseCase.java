package com.project.application.port.in;

import com.project.domain.model.Equipo;

public interface UpdateEquipoUseCase {

    record UpdateEquipoCommand(
            Long id,
            String nombre,
            String temporada,
            String liga,
            String descripcion,
            Long requestingUserId
    ) {}

    Equipo update(UpdateEquipoCommand command);
}
