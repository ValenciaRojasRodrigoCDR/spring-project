package com.project.domain.exception;

public class JugadorNotFoundException extends RuntimeException {
    public JugadorNotFoundException(Long id) {
        super("Jugador no encontrado: " + id);
    }
}
