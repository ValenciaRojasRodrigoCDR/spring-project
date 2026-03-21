package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.LoginUseCase;
import com.project.domain.exception.InvalidCredentialsException;
import com.project.infrastructure.adapter.in.web.dto.LoginRequest;
import com.project.infrastructure.adapter.in.web.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = loginUseCase.login(
                    new LoginUseCase.LoginCommand(request.username(), request.password())
            );
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
