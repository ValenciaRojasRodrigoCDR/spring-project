package com.project.infrastructure.adapter.in.web;

import com.project.application.port.in.GetUserQuery;
import com.project.domain.exception.UserNotFoundException;
import com.project.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock GetUserQuery getUserQuery;
    @InjectMocks UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    private User buildUser() {
        return User.builder().id(1L).username("admin").password("pass")
                .role("ADMIN").nombre("Admin").apellidos("Test").email("a@b.com")
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
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("a@b.com"));
    }

    @Test
    void me_userNotFound_returns404() throws Exception {
        when(getUserQuery.getByUsername("admin")).thenThrow(new UserNotFoundException("admin"));

        mockMvc.perform(get("/api/users/me").principal(mockAuth()))
                .andExpect(status().isNotFound());
    }
}
