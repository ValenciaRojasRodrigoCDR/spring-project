package com.project.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    // Base64 encoded secret, at least 256 bits for HS256
    private static final String SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLW11c3QtYmUtbG9uZy1lbm91Z2g=";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("admin", "ADMIN");

        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("admin", "ADMIN");

        String username = jwtUtil.extractUsername(token);

        assertThat(username).isEqualTo("admin");
    }

    @Test
    void isValid_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("admin", "ADMIN");

        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void isValid_invalidToken_returnsFalse() {
        assertThat(jwtUtil.isValid("this.is.not.valid")).isFalse();
    }

    @Test
    void isValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("admin", "ADMIN");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtUtil.generateToken("user1", "USER");
        String token2 = jwtUtil.generateToken("user2", "USER");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("admin", "ADMIN");

        String role = jwtUtil.extractRole(token);

        assertThat(role).isEqualTo("ADMIN");
    }
}
