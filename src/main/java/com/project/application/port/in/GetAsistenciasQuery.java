package com.project.application.port.in;

import com.project.domain.model.Asistencia;

import java.util.List;

public interface GetAsistenciasQuery {
    List<Asistencia> getByPartidoId(Long partidoId);
}
