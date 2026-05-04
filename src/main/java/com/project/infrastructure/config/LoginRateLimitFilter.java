package com.project.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private record BucketEntry(Bucket bucket, Instant lastAccess) {}

    private final ConcurrentHashMap<String, BucketEntry> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(request);
        BucketEntry entry = buckets.compute(ip, (k, existing) -> {
            if (existing == null) return new BucketEntry(newBucket(), Instant.now());
            return new BucketEntry(existing.bucket(), Instant.now());
        });

        if (entry.bucket().tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Maximos de intentos alcanzados, intentelo mas tarde\"}");
        }
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/api/auth/login".equals(request.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_ATTEMPTS)
                .refillIntervally(MAX_ATTEMPTS, BLOCK_DURATION)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Scheduled(fixedDelay = 1_800_000)
    public void limpiarBucketsInactivos() {
        Instant limite = Instant.now().minus(BLOCK_DURATION.multipliedBy(2));
        buckets.entrySet().removeIf(e -> e.getValue().lastAccess().isBefore(limite));
    }
}
