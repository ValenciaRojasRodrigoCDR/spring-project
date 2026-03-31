package com.project.domain.exception;

public class UnauthorizedEquipoAccessException extends RuntimeException {
    public UnauthorizedEquipoAccessException() {
        super("No tienes permiso para modificar este equipo");
    }
}
