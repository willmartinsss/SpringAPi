package jala.university.ds3.service;

import jala.university.ds3.domain.user.User;
import jala.university.ds3.domain.user.UserRole;
import jala.university.ds3.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;
    private AuthenticationManager authManager;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        authManager = mock(AuthenticationManager.class);
        userRepository = mock(UserRepository.class);
        authService = new AuthService(authManager, userRepository);
    }

    @Test
    @DisplayName("Should authenticate successfully with valid credentials")
    void testAuthenticateSuccess() throws AuthenticationException {
        // Given
        User user = new User("Admin", "admin", "encrypted", UserRole.ADMIN);
        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(user));
        doNothing().when(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // When
        String token = authService.authenticate("admin", "password");

        // Then
        assertNotNull(token);
    }

    @Test
    @DisplayName("Should throw exception with invalid username")
    void testAuthenticateInvalidUsername() throws AuthenticationException {
        when(userRepository.findByLogin("invalid")).thenReturn(Optional.empty());
        doNothing().when(authManager).authenticate(any());

        assertThrows(RuntimeException.class, () -> authService.authenticate("invalid", "password"));
    }

    @Test
    @DisplayName("Should throw exception with invalid password")
    void testAuthenticateInvalidPassword() throws AuthenticationException {
        doThrow(AuthenticationException.class)
                .when(authManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(AuthenticationException.class, () -> authService.authenticate("admin", "wrong"));
    }

    @Test
    @DisplayName("Should throw exception with null credentials")
    void testAuthenticateNullCredentials() throws AuthenticationException {
        doThrow(AuthenticationException.class).when(authManager).authenticate(any());

        assertThrows(AuthenticationException.class, () -> authService.authenticate(null, null));
    }

    @Test
    @DisplayName("Should throw exception with empty credentials")
    void testAuthenticateEmptyCredentials() throws AuthenticationException {
        doThrow(AuthenticationException.class).when(authManager).authenticate(any());

        assertThrows(AuthenticationException.class, () -> authService.authenticate("", ""));
    }

    @Test
    @DisplayName("Should be case sensitive for username")
    void testAuthenticateCaseSensitiveUsername() throws AuthenticationException {
        doThrow(AuthenticationException.class).when(authManager).authenticate(any());

        assertThrows(AuthenticationException.class, () -> authService.authenticate("ADMIN", "password"));
    }

    @Test
    @DisplayName("Should be case sensitive for password")
    void testAuthenticateCaseSensitivePassword() throws AuthenticationException {
        doThrow(AuthenticationException.class).when(authManager).authenticate(any());

        assertThrows(AuthenticationException.class, () -> authService.authenticate("admin", "PASSWORD"));
    }
}
