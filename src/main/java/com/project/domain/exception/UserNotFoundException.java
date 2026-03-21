package com.project.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Usuario no encontrado: " + username);
    }
}
