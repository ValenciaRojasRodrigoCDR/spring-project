package com.project.infrastructure.adapter.out.storage;

import com.project.application.port.out.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Component
public class FileStorageAdapter implements FileStoragePort {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path dir = base.resolve(folder).normalize();
            Path dest = dir.resolve(filename).normalize();

            if (!dest.startsWith(base)) {
                throw new IllegalArgumentException("Ruta de fichero no permitida");
            }

            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dest);
            return folder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el fichero", e);
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null) {
            throw new IllegalArgumentException("Tipo de fichero no permitido: nombre nulo");
        }
        int dot = originalFilename.lastIndexOf(".");
        if (dot < 0) {
            throw new IllegalArgumentException("Tipo de fichero no permitido: sin extensión");
        }
        String ext = originalFilename.substring(dot).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Tipo de fichero no permitido: " + ext);
        }
        return ext;
    }
}
