package com.project.application.port.in;

import java.util.List;

public interface AddEquipoToLigaUseCase {
    void addEquipo(Long ligaId, Long equipoId, Long requestingUserId);
    void removeEquipo(Long ligaId, Long equipoId, Long requestingUserId);
    List<Long> getEquipoIds(Long ligaId);
}
