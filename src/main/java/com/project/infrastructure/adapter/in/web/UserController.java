package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.GetUserQuery;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserQuery getUserQuery;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        try {
            User user = getUserQuery.getByUsername(authentication.getName());
            return ResponseEntity.ok(toResponse(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getNombre(),
                user.getApellidos(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
