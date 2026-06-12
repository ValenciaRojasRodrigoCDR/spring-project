package com.project.infrastructure.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF disabled intentionally: stateless JWT API uses Bearer tokens, not cookies — CSRF attacks do not apply
                // codeql[java/spring-disabled-csrf-protection]
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ── Rutas públicas ────────────────────────────────────────────────
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/jugadores/*/foto").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(
                                "/", "/login.html", "/index.html", "/profile.html",
                                "/import-club.html", "/club.html", "/estadisticas.html",
                                "/estadisticas-avanzadas.html",
                                "/jugadores.html", "/editar-jugador.html",
                                "/ligas.html", "/partidos.html", "/import-liga.html",
                                "/usuarios.html", "/landing.html",
                                "/css/**", "/js/**", "/assets/**", "/uploads/**"
                        ).permitAll()

                        // ── Gestión de usuarios (solo ADMIN) ──────────────────────────────
                        .requestMatchers("/api/users/create").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,  "/api/users/**").hasRole("ADMIN")

                        // ── Ligas: mutaciones (ADMIN | LIGA_OWNER) ────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/ligas").hasAnyRole("ADMIN", "LIGA_OWNER")
                        .requestMatchers(HttpMethod.PUT,    "/api/ligas/**").hasAnyRole("ADMIN", "LIGA_OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/ligas/**").hasAnyRole("ADMIN", "LIGA_OWNER")
                        .requestMatchers(HttpMethod.POST,   "/api/ligas/**").hasAnyRole("ADMIN", "LIGA_OWNER")

                        // ── Equipos: mutaciones (ADMIN | EQUIPO_OWNER) ────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/equipos").hasAnyRole("ADMIN", "EQUIPO_OWNER")
                        .requestMatchers(HttpMethod.PUT,  "/api/equipos/**").hasAnyRole("ADMIN", "EQUIPO_OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/equipos/importar").hasAnyRole("ADMIN", "EQUIPO_OWNER")

                        // ── Jugadores: crear (ADMIN | EQUIPO_OWNER); editar en controller ─
                        .requestMatchers(HttpMethod.POST, "/api/jugadores").hasAnyRole("ADMIN", "EQUIPO_OWNER")
                        .requestMatchers(HttpMethod.PUT,  "/api/jugadores/**")
                                .hasAnyRole("ADMIN", "EQUIPO_OWNER", "JUGADOR")

                        // ── Excel: importar (ADMIN | EQUIPO_OWNER) ────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/excel/**").hasAnyRole("ADMIN", "EQUIPO_OWNER")

                        // ── Lectura: cualquier autenticado ────────────────────────────────
                        .anyRequest().authenticated()
                )
                // Sin token válido → 401 para que el frontend redirija a login
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
