package com.project.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserNotFoundExceptionTest {

    @Test
    void constructor_setsMessageWithUsername() {
        UserNotFoundException ex = new UserNotFoundException("testuser");

        assertThat(ex.getMessage()).contains("testuser");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
