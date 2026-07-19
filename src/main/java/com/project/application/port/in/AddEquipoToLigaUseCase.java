package com.project.application.port.in;

import java.util.List;
import java.util.Map;

public interface AddEquipoToLigaUseCase {
    void addEquipo(Long ligaId, Long equipoId, Long requestingUserId);
    void removeEquipo(Long ligaId, Long equipoId, Long requestingUserId);
    List<Long> getEquipoIds(Long ligaId);
    Map<Long, List<Long>> getEquipoIdsByLigaIds(List<Long> ligaIds);
}
