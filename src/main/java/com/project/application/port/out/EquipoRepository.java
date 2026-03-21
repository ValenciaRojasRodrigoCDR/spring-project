package com.project.application.port.out;

import com.project.domain.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository {
    Equipo save(Equipo equipo);
    Optional<Equipo> findById(Long id);
    List<Equipo> findByUserId(Long userId);
}
