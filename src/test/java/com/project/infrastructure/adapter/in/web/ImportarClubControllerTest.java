package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.port.in.GetUserQuery;
import com.project.application.service.AsyncImportarClubService;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.ImportarClubRequest;
import com.project.infrastructure.store.ImportJobStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImportarClubControllerTest {

    @Mock AsyncImportarClubService asyncImportarClubService;
    @Mock GetUserQuery getUserQuery;
    @Mock ImportJobStore importJobStore;
    @InjectMocks ImportarClubController importarClubController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(importarClubController).build();
    }

    private UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken("admin", null, List.of());
    }

    @Test
    void importar_returns202_conJobId() throws Exception {
        User user = User.builder().id(1L).username("admin").password("p")
                .role("ADMIN").nombre("A").apellidos("B").email("a@b.com").build();

        when(getUserQuery.getByUsername("admin")).thenReturn(user);
        when(importJobStore.crear()).thenReturn("test-job-id");

        ImportarClubRequest request = new ImportarClubRequest("FC", "2024", "L1", "D",
                List.of(new ImportarClubRequest.JugadorDto("Leo", 5, 10, 0.5)));

        mockMvc.perform(post("/api/equipos/importar")
                        .principal(mockAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("test-job-id"));

        verify(asyncImportarClubService).importarAsync(eq("test-job-id"), any());
    }

    @Test
    void estado_pending_devuelvePending() throws Exception {
        when(importJobStore.obtener("abc")).thenReturn(
                Optional.of(new ImportJobStore.Job(ImportJobStore.Estado.PENDING, null, null, java.time.Instant.now())));

        mockMvc.perform(get("/api/equipos/importar/abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDING"));
    }

    @Test
    void estado_notFound_devuelve404() throws Exception {
        when(importJobStore.obtener("xyz")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/equipos/importar/xyz"))
                .andExpect(status().isNotFound());
    }
}
