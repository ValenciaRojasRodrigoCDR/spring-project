package com.project.infrastructure.adapter.out.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageAdapterTest {

    @TempDir
    Path tempDir;

    private FileStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FileStorageAdapter();
        ReflectionTestUtils.setField(adapter, "uploadDir", tempDir.toString());
    }

    @Test
    void store_validJpeg_returnsRelativePath() {
        MockMultipartFile file = new MockMultipartFile("foto", "player.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String result = adapter.store(file, "jugadores");

        assertThat(result).startsWith("jugadores/").endsWith(".jpg");
    }

    @Test
    void store_validPng_returnsRelativePath() {
        MockMultipartFile file = new MockMultipartFile("foto", "player.png", "image/png", new byte[]{1, 2, 3});

        String result = adapter.store(file, "jugadores");

        assertThat(result).startsWith("jugadores/").endsWith(".png");
    }

    @Test
    void store_validWebp_returnsRelativePath() {
        MockMultipartFile file = new MockMultipartFile("foto", "player.webp", "image/webp", new byte[]{1, 2, 3});

        String result = adapter.store(file, "jugadores");

        assertThat(result).startsWith("jugadores/").endsWith(".webp");
    }

    @Test
    void store_extensionCaseInsensitive_accepted() {
        MockMultipartFile file = new MockMultipartFile("foto", "player.JPEG", "image/jpeg", new byte[]{1, 2, 3});

        String result = adapter.store(file, "jugadores");

        assertThat(result).endsWith(".jpeg");
    }

    @Test
    void store_disallowedExtension_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile("foto", "malware.exe", "application/octet-stream", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> adapter.store(file, "jugadores"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de fichero no permitido");
    }

    @Test
    void store_noExtension_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile("foto", "noextension", "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> adapter.store(file, "jugadores"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void store_nullOriginalFilename_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile("foto", null, "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> adapter.store(file, "jugadores"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no permitido");
    }

    @Test
    void store_pathTraversalInFolder_throwsIllegalArgument() {
        MockMultipartFile file = new MockMultipartFile("foto", "player.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> adapter.store(file, "../../etc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no permitida");
    }

    @Test
    void store_createsFileOnDisk() throws IOException {
        MockMultipartFile file = new MockMultipartFile("foto", "player.jpg", "image/jpeg", new byte[]{10, 20, 30});

        String result = adapter.store(file, "jugadores");

        Path stored = tempDir.resolve(result);
        assertThat(stored).exists();
        assertThat(stored.toFile().length()).isEqualTo(3);
    }

    @Test
    void store_eachCallGeneratesUniqueFilename() {
        MockMultipartFile file = new MockMultipartFile("foto", "player.jpg", "image/jpeg", new byte[]{1});

        String result1 = adapter.store(file, "jugadores");
        String result2 = adapter.store(file, "jugadores");

        assertThat(result1).isNotEqualTo(result2);
    }
}
