package com.project.application.port.in;

import com.project.domain.model.Partido;

import java.util.List;

public interface GetPartidosQuery {
    List<Partido> getByLigaId(Long ligaId);
    List<Partido> getByEquipoId(Long equipoId);
    Partido getById(Long id);
}
