package com.project.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidCredentialsExceptionTest {

    @Test
    void constructor_setsMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        assertThat(ex.getMessage()).isEqualTo("Credenciales inválidas");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
