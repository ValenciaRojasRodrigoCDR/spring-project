package com.project.domain.exception;

public class PartidoNotFoundException extends RuntimeException {
    public PartidoNotFoundException(Long id) {
        super("Partido no encontrado: " + id);
    }
}
