package com.project.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.application.port.in.CreateUserUseCase;
import com.project.application.port.in.GetUserQuery;
import com.project.application.service.UserManagementService;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import com.project.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.project.infrastructure.adapter.in.web.dto.UpdateUserRoleRequest;
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

import java.time.LocalDateTime;
import java.util.List;

import com.project.application.port.in.CreateUserUseCase.CreateUserCommand;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock GetUserQuery getUserQuery;
    @Mock CreateUserUseCase createUserUseCase;
    @Mock UserManagementService userManagementService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Explicit construction avoids @InjectMocks type-ambiguity:
        // UserManagementService also implements CreateUserUseCase, so Mockito
        // would inject the wrong mock into the CreateUserUseCase field.
        UserController userController = new UserController(getUserQuery, createUserUseCase, userManagementService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    private User buildUser() {
        return User.builder().id(1L).username("admin").password("pass")
                .role("ROLE_ADMIN").nombre("Admin").apellidos("Test").email("a@b.com")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0)).build();
    }

    private UsernamePasswordAuthenticationToken mockAuth() {
        return new UsernamePasswordAuthenticationToken("admin", null, List.of());
    }

    @Test
    void me_success_returnsUserResponse() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenReturn(buildUser());

        mockMvc.perform(get("/api/users/me").principal(mockAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    void me_userNotFound_returns404() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenThrow(new UserNotFoundException("admin"));

        mockMvc.perform(get("/api/users/me").principal(mockAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAll_returnsAllUsers() throws Exception {
        when(userManagementService.getAll()).thenReturn(List.of(buildUser(),
                User.builder().id(2L).username("user2").password("p").role("ROLE_USER")
                        .nombre("B").apellidos("C").email("b@c.com").build()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    void listAll_emptyList_returnsEmptyArray() throws Exception {
        when(userManagementService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        when(createUserUseCase.create(any(CreateUserCommand.class))).thenReturn(buildUser());

        var req = new CreateUserRequest("admin", "pass", "ROLE_ADMIN", "Admin", "Test", "a@b.com", null);
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void updateRole_validRequest_returns200() throws Exception {
        User updated = User.builder().id(1L).username("admin").password("p")
                .role("ROLE_LIGA_OWNER").nombre("Admin").apellidos("Test").email("a@b.com").build();
        when(userManagementService.updateRole(eq(1L), eq("ROLE_LIGA_OWNER"), eq(5L))).thenReturn(updated);

        var req = new UpdateUserRoleRequest("ROLE_LIGA_OWNER", 5L);
        mockMvc.perform(put("/api/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_LIGA_OWNER"));
    }
}
