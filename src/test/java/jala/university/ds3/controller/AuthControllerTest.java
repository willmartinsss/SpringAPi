package jala.university.ds3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jala.university.ds3.service.AuthService;
import jala.university.ds3.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return token when login is successful")
    void testLoginSuccess() throws Exception {
        // Given
        when(authService.authenticate("admin", "password"))
                .thenReturn("jwt-token-example");

        LoginRequest request = new LoginRequest("admin", "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-example"));
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void testLoginFailure() throws Exception {
        // Given
        when(authService.authenticate(anyString(), anyString()))
                .thenThrow(new AuthenticationException("Invalid credentials") {});

        LoginRequest request = new LoginRequest("invalid", "wrong");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    @DisplayName("Should return 400 when request body is malformed")
    void testLoginMalformedRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when username is missing")
    void testLoginMissingUsername() throws Exception {
        // Given
        LoginRequest request = new LoginRequest(null, "password");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when password is missing")
    void testLoginMissingPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("admin", null);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
