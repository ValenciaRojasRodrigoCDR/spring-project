package com.project.infrastructure.store;

import com.project.application.port.in.ImportarClubUseCase.ImportarClubResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImportJobStore {

    public enum Estado { PENDING, DONE, FAILED }

    public record Job(Estado estado, ImportarClubResult resultado, String error, Instant createdAt) {}

    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    public String crear() {
        String id = UUID.randomUUID().toString();
        jobs.put(id, new Job(Estado.PENDING, null, null, Instant.now()));
        return id;
    }

    public void completar(String id, ImportarClubResult resultado) {
        jobs.computeIfPresent(id, (k, old) ->
                new Job(Estado.DONE, resultado, null, old.createdAt()));
    }

    public void fallar(String id, String error) {
        jobs.computeIfPresent(id, (k, old) ->
                new Job(Estado.FAILED, null, error, old.createdAt()));
    }

    public Optional<Job> obtener(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void limpiarJobsAntiguos() {
        Instant limite = Instant.now().minusSeconds(86_400);
        jobs.entrySet().removeIf(e -> e.getValue().createdAt().isBefore(limite));
    }
}
