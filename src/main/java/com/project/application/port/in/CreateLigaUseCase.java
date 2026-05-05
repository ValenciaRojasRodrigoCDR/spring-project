package com.project.application.port.in;

import com.project.domain.model.Liga;

public interface CreateLigaUseCase {
    Liga create(CreateLigaCommand command);

    record CreateLigaCommand(String nombre, String temporada, String descripcion, Long userId) {}
}
