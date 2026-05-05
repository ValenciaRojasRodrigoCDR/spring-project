package com.project.application.port.in;

import com.project.domain.model.Asistencia;

import java.util.List;

public interface RegistrarAsistenciaUseCase {
    List<Asistencia> registrar(Long partidoId, List<AsistenciaItem> items);

    record AsistenciaItem(Long jugadorId, boolean asistio, int goles, int minutos, boolean titular) {}
}
