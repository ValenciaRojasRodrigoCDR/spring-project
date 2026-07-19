package com.project.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JugadorJpaRepository extends JpaRepository<JugadorEntity, Long> {
    List<JugadorEntity> findByEquipoId(Long equipoId);
    List<JugadorEntity> findByEquipoId(Long equipoId, Pageable pageable);
    long countByEquipoId(Long equipoId);
}
