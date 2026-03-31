package com.project.domain.exception;

public class EquipoNotFoundException extends RuntimeException {
    public EquipoNotFoundException(Long id) {
        super("Equipo no encontrado: " + id);
    }
}
