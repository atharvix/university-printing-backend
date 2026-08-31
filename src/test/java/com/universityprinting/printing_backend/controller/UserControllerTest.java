package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.universityprinting.printing_backend.dto.CreateUserRequest;
import com.universityprinting.printing_backend.dto.UserResponse;
import com.universityprinting.printing_backend.exception.DuplicateEmailException;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.service.UserService;
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
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void createUser_Success_Returns201Created() throws Exception {
        Instant now = Instant.now();
        UserResponse response = new UserResponse(
            "user-123",
            "Alice Smith",
            "alice@university.edu",
            "+1234567890",
            Role.STUDENT,
            now,
            now
        );

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        String jsonPayload = """
            {
                "name": "Alice Smith",
                "email": "alice@university.edu",
                "phone": "+1234567890"
            }
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-123"))
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@university.edu"))
                .andExpect(jsonPath("$.phone").value("+1234567890"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void createUser_DuplicateEmail_Returns409Conflict() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
            .thenThrow(new DuplicateEmailException("User with email 'alice@university.edu' already exists"));

        String jsonPayload = """
            {
                "name": "Alice Smith",
                "email": "alice@university.edu",
                "phone": "+1234567890"
            }
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with email 'alice@university.edu' already exists"));
    }

    @Test
    void createUser_InvalidInput_Returns400BadRequest() throws Exception {
        String invalidJsonPayload = """
            {
                "name": "",
                "email": "invalid-email-format",
                "phone": ""
            }
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.name").exists())
                .andExpect(jsonPath("$.details.email").exists())
                .andExpect(jsonPath("$.details.phone").exists());
    }
}
