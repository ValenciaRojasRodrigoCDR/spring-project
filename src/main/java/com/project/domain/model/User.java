package com.project.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class User {
    Long id;
    String username;
    String password;
    String role;
}
