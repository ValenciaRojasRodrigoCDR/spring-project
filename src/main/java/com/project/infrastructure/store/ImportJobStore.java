package com.project.infrastructure.store;

import com.project.application.port.in.ImportarClubUseCase.ImportarClubResult;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ImportJobStore {

    public enum Estado { PENDING, DONE, FAILED }

    public record Job(Estado estado, ImportarClubResult resultado, String error) {}

    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();

    public String crear() {
        String id = UUID.randomUUID().toString();
        jobs.put(id, new Job(Estado.PENDING, null, null));
        return id;
    }

    public void completar(String id, ImportarClubResult resultado) {
        jobs.put(id, new Job(Estado.DONE, resultado, null));
    }

    public void fallar(String id, String error) {
        jobs.put(id, new Job(Estado.FAILED, null, error));
    }

    public Optional<Job> obtener(String id) {
        return Optional.ofNullable(jobs.get(id));
    }
}
