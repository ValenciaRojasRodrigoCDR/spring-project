package com.project.domain.exception;

public class UnauthorizedLigaAccessException extends RuntimeException {
    public UnauthorizedLigaAccessException() {
        super("No tienes permiso para acceder a esta liga");
    }
}
