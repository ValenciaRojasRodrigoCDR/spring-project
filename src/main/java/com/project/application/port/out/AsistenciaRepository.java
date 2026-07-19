package com.project.application.port.out;

import com.project.domain.model.Asistencia;

import java.util.List;

public interface AsistenciaRepository {
    Asistencia save(Asistencia asistencia);
    List<Asistencia> saveAll(List<Asistencia> asistencias);
    List<Asistencia> findByPartidoId(Long partidoId);
    void deleteByPartidoId(Long partidoId);
}
