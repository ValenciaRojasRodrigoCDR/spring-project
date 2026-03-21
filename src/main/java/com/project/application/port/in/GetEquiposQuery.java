package com.project.application.port.in;

import com.project.domain.model.Equipo;

import java.util.List;

public interface GetEquiposQuery {
    List<Equipo> getByUserId(Long userId);
}
