package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.universityprinting.printing_backend.dto.AuthResponse;
import com.universityprinting.printing_backend.dto.LoginRequest;
import com.universityprinting.printing_backend.dto.RegisterRequest;
import com.universityprinting.printing_backend.dto.UserResponse;
import com.universityprinting.printing_backend.exception.AuthenticationFailedException;
import com.universityprinting.printing_backend.exception.DuplicateEmailException;
import com.universityprinting.printing_backend.exception.GlobalExceptionHandler;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.service.AuthService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthController authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void register_Success_Returns201Created() throws Exception {
        Instant now = Instant.now();
        UserResponse userResponse = new UserResponse(
            "user-1",
            "Charlie Brown",
            "charlie@university.edu",
            "+1234567890",
            Role.STUDENT,
            now,
            now
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        String jsonPayload = """
            {
                "name": "Charlie Brown",
                "email": "charlie@university.edu",
                "phone": "+1234567890",
                "password": "SecurePassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.name").value("Charlie Brown"))
                .andExpect(jsonPath("$.email").value("charlie@university.edu"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_DuplicateEmail_Returns409Conflict() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new DuplicateEmailException("User with email 'charlie@university.edu' already exists"));

        String jsonPayload = """
            {
                "name": "Charlie Brown",
                "email": "charlie@university.edu",
                "phone": "+1234567890",
                "password": "SecurePassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User with email 'charlie@university.edu' already exists"));
    }

    @Test
    void register_PasswordShorterThan8_Returns400BadRequest() throws Exception {
        String jsonPayload = """
            {
                "name": "Charlie Brown",
                "email": "charlie@university.edu",
                "phone": "+1234567890",
                "password": "short"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    void register_InvalidEmail_Returns400BadRequest() throws Exception {
        String jsonPayload = """
            {
                "name": "Charlie Brown",
                "email": "invalid-email",
                "phone": "+1234567890",
                "password": "ValidPassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.email").exists());
    }

    @Test
    void login_Success_Returns200AndToken() throws Exception {
        Instant now = Instant.now();
        UserResponse userResponse = new UserResponse(
            "user-1",
            "Charlie Brown",
            "charlie@university.edu",
            "+1234567890",
            Role.STUDENT,
            now,
            now
        );
        AuthResponse authResponse = AuthResponse.of("mock.jwt.token", 3600L, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        String jsonPayload = """
            {
                "email": "charlie@university.edu",
                "password": "SecurePassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.user.id").value("user-1"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void login_InvalidCredentials_Returns401Unauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new AuthenticationFailedException("Invalid email or password"));

        String jsonPayload = """
            {
                "email": "charlie@university.edu",
                "password": "WrongPassword123"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Authentication failed"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
