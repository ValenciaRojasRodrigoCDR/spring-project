package com.project.application.port.in;

import com.project.domain.model.Liga;

public interface UpdateLigaUseCase {
    Liga update(UpdateLigaCommand command);

    record UpdateLigaCommand(Long id, String nombre, String temporada, String descripcion, Long requestingUserId) {}
}
