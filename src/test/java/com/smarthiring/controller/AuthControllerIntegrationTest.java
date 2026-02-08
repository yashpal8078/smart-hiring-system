package com.smarthiring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthiring.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev") // Use dev profile (Ensure H2 or separate test DB is used in real prod)
@Transactional // Rollback transaction after each test
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Integration Test User")
                .email("integration@test.com")
                .password("Pass@1234")
                .confirmPassword("Pass@1234")
                .phone("1234567890")
                .role("CANDIDATE")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.email").value("integration@test.com"));
    }

    @Test
    void shouldFailRegisterWithExistingEmail() throws Exception {
        // First register (using existing admin email from data.sql if available, or create one)
        // We assume admin@smarthiring.com exists from data.sql

        RegisterRequest request = RegisterRequest.builder()
                .fullName("Duplicate User")
                .email("admin@smarthiring.com")
                .password("Pass@1234")
                .confirmPassword("Pass@1234")
                .phone("1234567890")
                .role("CANDIDATE")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}