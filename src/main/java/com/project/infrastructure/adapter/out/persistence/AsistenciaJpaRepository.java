package com.project.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsistenciaJpaRepository extends JpaRepository<AsistenciaEntity, Long> {
    List<AsistenciaEntity> findByPartidoId(Long partidoId);
    List<AsistenciaEntity> findByJugadorId(Long jugadorId);
    void deleteByPartidoId(Long partidoId);
}
