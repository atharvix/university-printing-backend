package com.universityprinting.printing_backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.universityprinting.printing_backend.dto.CreateUserRequest;
import com.universityprinting.printing_backend.dto.UserResponse;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.service.UserService;
import java.time.Instant;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "spring.mongodb.uri=mongodb://localhost:27017/test_db")
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void healthEndpoint_IsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void authRegisterEndpoint_IsPubliclyAccessible() throws Exception {
        String invalidPayload = "{}";

        // Should reach validation/controller without requiring authentication (returns 400 Bad Request, not 401)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpoint_WithoutToken_Returns401Unauthorized() throws Exception {
        String payload = """
            {
                "name": "David Admin",
                "email": "david@university.edu",
                "phone": "+1234567890"
            }
            """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void protectedAdminEndpoint_WithStudentToken_Returns403Forbidden() throws Exception {
        User student = new User("student-1", "Student User", "student@university.edu", "+1234567890", Role.STUDENT, "hash", Instant.now(), Instant.now());
        String studentToken = jwtService.generateToken(student);

        String payload = """
            {
                "name": "David Admin",
                "email": "david@university.edu",
                "phone": "+1234567890"
            }
            """;

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void protectedAdminEndpoint_WithAdminToken_Returns201Created() throws Exception {
        User admin = new User("admin-1", "Admin User", "admin@university.edu", "+1234567890", Role.ADMIN, "hash", Instant.now(), Instant.now());
        String adminToken = jwtService.generateToken(admin);

        UserResponse userResponse = new UserResponse("user-new", "David Admin", "david@university.edu", "+1234567890", Role.STUDENT, Instant.now(), Instant.now());
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(userResponse);

        String payload = """
            {
                "name": "David Admin",
                "email": "david@university.edu",
                "phone": "+1234567890"
            }
            """;

        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("user-new"));
    }
}
