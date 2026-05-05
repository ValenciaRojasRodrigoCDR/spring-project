package com.project.domain.exception;

public class LigaNotFoundException extends RuntimeException {
    public LigaNotFoundException(Long id) {
        super("Liga no encontrada: " + id);
    }
}
