package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.universityprinting.printing_backend.dto.AuthResponse;
import com.universityprinting.printing_backend.dto.LoginRequest;
import com.universityprinting.printing_backend.dto.RegisterRequest;
import com.universityprinting.printing_backend.dto.UserResponse;
import com.universityprinting.printing_backend.exception.AuthenticationFailedException;
import com.universityprinting.printing_backend.exception.DuplicateEmailException;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_Success_HashesPasswordAndAssignsStudentRole() {
        RegisterRequest request = new RegisterRequest("Bob Builder", "bob@university.edu", "+1987654321", "SecretPass123");

        when(userRepository.existsByEmail("bob@university.edu")).thenReturn(false);
        when(passwordEncoder.encode("SecretPass123")).thenReturn("$2a$10$hashedPasswordValue");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("user-abc-123");
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("user-abc-123", response.id());
        assertEquals("Bob Builder", response.name());
        assertEquals("bob@university.edu", response.email());
        assertEquals("+1987654321", response.phone());
        assertEquals(Role.STUDENT, response.role());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("$2a$10$hashedPasswordValue", savedUser.getPasswordHash());
        assertEquals(Role.STUDENT, savedUser.getRole());
    }

    @Test
    void register_DuplicateEmail_ThrowsDuplicateEmailException() {
        RegisterRequest request = new RegisterRequest("Bob Builder", "bob@university.edu", "+1987654321", "SecretPass123");

        when(userRepository.existsByEmail("bob@university.edu")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success_ReturnsJwtAndSafeUserResponse() {
        LoginRequest request = new LoginRequest("bob@university.edu", "SecretPass123");
        User user = new User(
            "user-123",
            "Bob Builder",
            "bob@university.edu",
            "+1987654321",
            Role.STUDENT,
            "$2a$10$hashedPasswordValue",
            Instant.now(),
            Instant.now()
        );

        when(userRepository.findByEmail("bob@university.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecretPass123", "$2a$10$hashedPasswordValue")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        assertNotNull(response.user());
        assertEquals("user-123", response.user().id());
        assertEquals("bob@university.edu", response.user().email());
    }

    @Test
    void login_NonExistentEmail_ThrowsAuthenticationFailedException() {
        LoginRequest request = new LoginRequest("nonexistent@university.edu", "SecretPass123");

        when(userRepository.findByEmail("nonexistent@university.edu")).thenReturn(Optional.empty());

        AuthenticationFailedException ex = assertThrows(
            AuthenticationFailedException.class,
            () -> authService.login(request)
        );
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_IncorrectPassword_ThrowsAuthenticationFailedException() {
        LoginRequest request = new LoginRequest("bob@university.edu", "WrongPassword");
        User user = new User(
            "user-123",
            "Bob Builder",
            "bob@university.edu",
            "+1987654321",
            Role.STUDENT,
            "$2a$10$hashedPasswordValue",
            Instant.now(),
            Instant.now()
        );

        when(userRepository.findByEmail("bob@university.edu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "$2a$10$hashedPasswordValue")).thenReturn(false);

        AuthenticationFailedException ex = assertThrows(
            AuthenticationFailedException.class,
            () -> authService.login(request)
        );
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
