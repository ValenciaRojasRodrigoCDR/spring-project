package com.project.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.project.infrastructure.adapter.in.web.dto.ImportarClubRequest;
import com.project.infrastructure.adapter.in.web.dto.LoginRequest;
import com.project.infrastructure.adapter.in.web.dto.LoginResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ImportarClubTimingTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static String token;
    private static final List<String> resultados = new ArrayList<>();

    @BeforeAll
    static void obtenerToken(@Autowired TestRestTemplate restTemplate) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var loginReq = new HttpEntity<>(new LoginRequest("admin", "changeme"), headers);
        ResponseEntity<LoginResponse> resp = restTemplate.postForEntity(
                "/api/auth/login", loginReq, LoginResponse.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        token = resp.getBody().token();
    }

    @Test @Order(1)
    void timing_1_jugador() throws Exception { medir(1); }

    @Test @Order(2)
    void timing_16_jugadores() throws Exception { medir(16); }

    @Test @Order(3)
    void timing_63_jugadores_stress() throws Exception { medir(63); }

    @Test @Order(4)
    void timing_100_jugadores() throws Exception { medir(100); }

    @Test @Order(5)
    void timing_500_jugadores() throws Exception { medir(500); }

    @AfterAll
    static void resumen() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   TIMING — POST /api/equipos/importar ║");
        System.out.println("╠══════════════════════════════════════╣");
        resultados.forEach(r -> System.out.println("║  " + r));
        System.out.println("╚══════════════════════════════════════╝\n");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void medir(int numJugadores) throws Exception {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        var entity = new HttpEntity<>(buildRequest(numJugadores), headers);

        // Warmup
        String warmupId = postYObtenJobId(entity);
        pollHastaDone(warmupId, headers);

        // Medición real (POST + polling hasta DONE)
        long inicio = System.nanoTime();
        String jobId = postYObtenJobId(entity);
        pollHastaDone(jobId, headers);
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        String linea = String.format("%-6d jugadores → %4d ms", numJugadores, ms);
        resultados.add(linea);
        System.out.println("[TIMING] " + linea);
    }

    @SuppressWarnings("unchecked")
    private String postYObtenJobId(HttpEntity<ImportarClubRequest> entity) {
        ResponseEntity<Map<String, String>> resp = restTemplate.exchange(
                "/api/equipos/importar", HttpMethod.POST, entity,
                new ParameterizedTypeReference<>() {});
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        return resp.getBody().get("jobId");
    }

    @SuppressWarnings("unchecked")
    private void pollHastaDone(String jobId, HttpHeaders headers) throws Exception {
        var pollEntity = new HttpEntity<>(headers);
        while (true) {
            Thread.sleep(10);
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    "/api/equipos/importar/" + jobId, HttpMethod.GET, pollEntity,
                    new ParameterizedTypeReference<>() {});
            String estado = (String) resp.getBody().get("estado");
            if ("DONE".equals(estado) || "FAILED".equals(estado)) break;
        }
    }

    private ImportarClubRequest buildRequest(int n) {
        List<ImportarClubRequest.JugadorDto> jugadores = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            jugadores.add(new ImportarClubRequest.JugadorDto(
                    "Jugador" + i,
                    (int) (Math.random() * 20),
                    (int) (Math.random() * 30) + 5,
                    Math.round(Math.random() * 10.0) / 10.0
            ));
        }
        return new ImportarClubRequest("Club Timing " + n + "j", "2024/25", "Liga Test",
                "Test de rendimiento", jugadores);
    }
}
