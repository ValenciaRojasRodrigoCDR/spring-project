package com.project.infrastructure.config;

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
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/jugadores/*/foto").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/", "/login.html", "/index.html", "/profile.html", "/import-club.html", "/club.html", "/estadisticas.html", "/jugadores.html", "/editar-jugador.html", "/css/**", "/js/**", "/assets/**").permitAll()
                        // Mutaciones — solo ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/jugadores").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/jugadores/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/equipos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/equipos/importar").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/excel/**").hasRole("ADMIN")
                        // Lectura — cualquier autenticado
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
