package com.taskforge.auth.service;

import com.taskforge.auth.dto.LoginRequest;
import com.taskforge.auth.dto.RegisterRequest;
import com.taskforge.auth.entity.User;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUserAndReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenProvider.generateAccessToken(any(UUID.class), eq("test@example.com"))).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(any(UUID.class))).thenReturn("refresh-token");

        AuthService.AuthTokens tokens = authService.register(request);

        assertNotNull(tokens);
        assertEquals("access-token", tokens.accessToken());
        assertEquals("refresh-token", tokens.refreshToken());
        assertEquals("Test User", tokens.user().getName());
        assertEquals("test@example.com", tokens.user().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnAuthResponseWithValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User("Test User", "test@example.com", "encoded");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user.getId(), "test@example.com")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(user.getId())).thenReturn("refresh-token");

        AuthService.AuthTokens tokens = authService.login(request);

        assertNotNull(tokens);
        assertEquals("access-token", tokens.accessToken());
    }

    @Test
    void login_shouldThrowUnauthorizedWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowUnauthorizedWhenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User("Test", "test@example.com", "encoded")));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.login(request));
    }

    @Test
    void refresh_shouldReturnNewTokens() {
        String refreshToken = "valid-refresh-token";
        User user = new User("Test User", "test@example.com", "encoded");
        UUID userId = user.getId();

        when(jwtTokenProvider.validateRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromRefreshToken("valid-refresh-token")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(userId, "test@example.com")).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(userId)).thenReturn("new-refresh-token");

        AuthService.AuthTokens tokens = authService.refresh(refreshToken);

        assertNotNull(tokens);
        assertEquals("new-access-token", tokens.accessToken());
    }

    @Test
    void refresh_shouldThrowUnauthorizedWhenTokenInvalid() {
        when(jwtTokenProvider.validateRefreshToken("invalid")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authService.refresh("invalid"));
    }
}
