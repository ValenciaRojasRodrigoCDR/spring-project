package com.project.infrastructure.adapter.out.storage;

import com.project.application.port.out.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileStorageAdapter implements FileStoragePort {

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file, String folder) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path dir = Paths.get(uploadDir, folder);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename));
            return folder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el fichero", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
