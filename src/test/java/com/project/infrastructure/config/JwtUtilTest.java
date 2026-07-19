package com.project.infrastructure.config;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET =
            "cHJvamVjdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW5zLWNoYW5nZS1pbi1wcm9kdWN0aW9u";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
        jwtUtil.init();
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("admin", "ADMIN", 1L, null);

        assertThat(token).isNotBlank();
    }

    @Test
    void parse_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("admin", "ADMIN", 1L, null);

        Claims claims = jwtUtil.parse(token);

        assertThat(claims.getSubject()).isEqualTo("admin");
    }

    @Test
    void parse_returnsRoleUserIdAndJugadorId() {
        String token = jwtUtil.generateToken("player1", "JUGADOR", 7L, 42L);

        Claims claims = jwtUtil.parse(token);

        assertThat(claims.get("role", String.class)).isEqualTo("JUGADOR");
        assertThat(claims.get("userId", Number.class).longValue()).isEqualTo(7L);
        assertThat(claims.get("jugadorId", Number.class).longValue()).isEqualTo(42L);
    }

    @Test
    void parse_withoutJugadorId_claimIsNull() {
        String token = jwtUtil.generateToken("admin", "ADMIN", 1L, null);

        Claims claims = jwtUtil.parse(token);

        assertThat(claims.get("jugadorId", Number.class)).isNull();
    }

    @Test
    void parse_invalidToken_returnsNull() {
        assertThat(jwtUtil.parse("this.is.not.valid")).isNull();
    }

    @Test
    void parse_tamperedToken_returnsNull() {
        String token = jwtUtil.generateToken("admin", "ADMIN", 1L, null);
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(jwtUtil.parse(tampered)).isNull();
    }

    @Test
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtUtil.generateToken("user1", "USER", 1L, null);
        String token2 = jwtUtil.generateToken("user2", "USER", 2L, null);

        assertThat(token1).isNotEqualTo(token2);
    }
}
