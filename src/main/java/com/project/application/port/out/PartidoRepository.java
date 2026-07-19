package com.project.application.port.out;

import com.project.domain.model.Partido;

import java.util.List;
import java.util.Optional;

public interface PartidoRepository {
    Partido save(Partido partido);
    List<Partido> saveAll(List<Partido> partidos);
    Optional<Partido> findById(Long id);
    boolean existsById(Long id);
    List<Partido> findByLigaId(Long ligaId);
    List<Partido> findByLigaId(Long ligaId, int page, int size);
    long countByLigaId(Long ligaId);
    List<Partido> findByEquipoId(Long equipoId);
    void deleteById(Long id);
}
