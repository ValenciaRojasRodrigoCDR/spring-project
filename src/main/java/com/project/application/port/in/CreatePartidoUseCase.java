package com.project.application.port.in;

import com.project.domain.model.Partido;

import java.time.LocalDate;

public interface CreatePartidoUseCase {
    Partido create(CreatePartidoCommand command);

    record CreatePartidoCommand(
            Long ligaId,
            Long equipoId,
            String rival,
            LocalDate fecha,
            String lugar,
            String resultado,
            int golesFavor,
            int golesContra
    ) {}
}
