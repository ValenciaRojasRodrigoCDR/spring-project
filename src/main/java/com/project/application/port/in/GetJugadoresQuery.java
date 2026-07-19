package com.project.application.port.in;

import com.project.domain.model.Jugador;

import java.util.List;

public interface GetJugadoresQuery {
    List<Jugador> getByEquipoId(Long equipoId);
    PageResult<Jugador> getByEquipoId(Long equipoId, int page, int size);
}
