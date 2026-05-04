package com.project.application.port.out;

import com.project.domain.model.Asistencia;

import java.util.List;

public interface AsistenciaRepository {
    Asistencia save(Asistencia asistencia);
    List<Asistencia> findByPartidoId(Long partidoId);
    List<Asistencia> findByJugadorId(Long jugadorId);
    void deleteByPartidoId(Long partidoId);
}
