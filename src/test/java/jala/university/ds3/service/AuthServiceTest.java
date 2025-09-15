package jala.university.ds3.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.AuthenticationException;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("Should authenticate successfully with valid credentials")
    void testAuthenticateSuccess() {
        // Given
        String username = "admin";
        String password = "password";

        // When
        String token = authService.authenticate(username, password);

        // Then
        assertNotNull(token);
        assertEquals("jwt-token-example", token);
    }

    @Test
    @DisplayName("Should throw exception with invalid username")
    void testAuthenticateInvalidUsername() {
        // Given
        String username = "invalid";
        String password = "password";

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(username, password);
        });
    }

    @Test
    @DisplayName("Should throw exception with invalid password")
    void testAuthenticateInvalidPassword() {
        // Given
        String username = "admin";
        String password = "wrong";

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(username, password);
        });
    }

    @Test
    @DisplayName("Should throw exception with null credentials")
    void testAuthenticateNullCredentials() {
        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(null, null);
        });
    }

    @Test
    @DisplayName("Should throw exception with empty credentials")
    void testAuthenticateEmptyCredentials() {
        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("", "");
        });
    }

    @Test
    @DisplayName("Should be case sensitive for username")
    void testAuthenticateCaseSensitiveUsername() {
        // Given
        String username = "ADMIN";
        String password = "password";

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(username, password);
        });
    }

    @Test
    @DisplayName("Should be case sensitive for password")
    void testAuthenticateCaseSensitivePassword() {
        // Given
        String username = "admin";
        String password = "PASSWORD";

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(username, password);
        });
    }
}
