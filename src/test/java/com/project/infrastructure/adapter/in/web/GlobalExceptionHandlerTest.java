package com.project.infrastructure.adapter.in.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @SuppressWarnings("unchecked")
    void handleValidation_singleError_returns400WithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "nombre", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("nombre", "must not be blank");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleValidation_multipleErrors_allIncluded() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "nombre", "must not be blank"),
                new FieldError("obj", "edad", "must be positive")
        ));

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).hasSize(2).containsKeys("nombre", "edad");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_nestedPath_extractsFieldName() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("create.nombre");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("nombre", "must not be blank");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleConstraintViolation_simplePath_usesPathAsKey() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("nombre");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertThat(errors).containsEntry("nombre", "must not be blank");
    }
}
