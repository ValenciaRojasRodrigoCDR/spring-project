package com.project.application.port.in;

import com.project.domain.model.User;

public interface CreateUserUseCase {
    User create(CreateUserCommand command);

    record CreateUserCommand(
            String username,
            String password,
            String role,
            String nombre,
            String apellidos,
            String email,
            Long jugadorId
    ) {}
}
