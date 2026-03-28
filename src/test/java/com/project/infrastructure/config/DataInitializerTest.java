package com.project.infrastructure.config;

import com.project.infrastructure.adapter.out.persistence.UserEntity;
import com.project.infrastructure.adapter.out.persistence.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock UserJpaRepository userJpaRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "testpassword");
    }

    @Test
    void run_adminNotExists_createsAdmin() throws Exception {
        when(userJpaRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testpassword")).thenReturn("encoded");

        dataInitializer.run();

        verify(userJpaRepository).save(any(UserEntity.class));
    }

    @Test
    void run_adminAlreadyExists_doesNotCreate() throws Exception {
        UserEntity existing = UserEntity.builder().id(1L).username("admin").build();
        when(userJpaRepository.findByUsername("admin")).thenReturn(Optional.of(existing));

        dataInitializer.run();

        verify(userJpaRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
