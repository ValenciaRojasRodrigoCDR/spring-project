package com.project.application.port.out;

import com.project.domain.model.Liga;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LigaRepository {
    Liga save(Liga liga);
    Optional<Liga> findById(Long id);
    List<Liga> findByUserId(Long userId);
    void deleteById(Long id);
    void addEquipo(Long ligaId, Long equipoId);
    void removeEquipo(Long ligaId, Long equipoId);
    List<Long> findEquipoIdsByLigaId(Long ligaId);
    Map<Long, List<Long>> findEquipoIdsByLigaIds(List<Long> ligaIds);
}
