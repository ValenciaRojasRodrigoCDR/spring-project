package com.project.application.port.out;

import com.project.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
}
