package com.project.application.port.in;

import com.project.domain.model.Equipo;

public interface CreateEquipoUseCase {

    record CreateEquipoCommand(String nombre, String temporada, String liga, String descripcion, Long userId) {}

    Equipo create(CreateEquipoCommand command);
}
