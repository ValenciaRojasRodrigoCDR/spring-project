package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.CreateUserUseCase;
import com.project.application.port.in.GetUserQuery;
import com.project.application.service.UserManagementService;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.project.infrastructure.adapter.in.web.dto.UpdateUserRoleRequest;
import com.project.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetUserQuery          getUserQuery;
    private final CreateUserUseCase     createUserUseCase;
    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        try {
            User user = getUserQuery.getByUsername(authentication.getName());
            return ResponseEntity.ok(toResponse(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listAll() {
        return ResponseEntity.ok(userManagementService.getAll().stream().map(this::toResponse).toList());
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User user = createUserUseCase.create(new CreateUserUseCase.CreateUserCommand(
                request.username(), request.password(), request.role(),
                request.nombre(), request.apellidos(), request.email(), request.jugadorId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRoleRequest request) {
        User user = userManagementService.updateRole(id, request.role(), request.jugadorId());
        return ResponseEntity.ok(toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getUsername(), user.getRole(),
                user.getNombre(), user.getApellidos(), user.getEmail(),
                user.getCreatedAt(), user.getJugadorId());
    }
}
