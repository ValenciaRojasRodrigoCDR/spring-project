package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class User {
    Long id;
    String username;
    String password;
    String role;
    String nombre;
    String apellidos;
    String email;
    LocalDateTime createdAt;
}
