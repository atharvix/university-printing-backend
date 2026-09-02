package com.universityprinting.printing_backend.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cloudinary.Cloudinary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.dto.CreatePrintJobRequest;
import com.universityprinting.printing_backend.dto.PrintJobResponse;
import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.service.PrintJobService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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
class PrintJobSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DocumentRepository documentRepository;

    @MockitoBean
    private PrintJobRepository printJobRepository;

    @MockitoBean
    private PrintJobService printJobService;

    @MockitoBean
    private Cloudinary cloudinary;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void createPrintJob_Unauthenticated_Returns401Unauthorized() throws Exception {
        CreatePrintJobRequest request = new CreatePrintJobRequest("doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5);

        mockMvc.perform(post("/api/print-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getMyPrintJobs_Unauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/print-jobs"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void createPrintJob_AuthenticatedStudent_Returns201Created() throws Exception {
        User user = new User("student-123", "Student User", "student@university.edu", "+1234567890", Role.STUDENT, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(user);

        CreatePrintJobRequest request = new CreatePrintJobRequest("doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5);
        PrintJobResponse response = new PrintJobResponse(
            "job-1", "student-123", "doc-1", 1, ColorMode.BLACK_WHITE, PaperSize.A4, false, 5,
            new BigDecimal("10.00"), PrintJobStatus.QUEUED, Instant.now(), Instant.now()
        );

        when(printJobService.createPrintJob(any(CreatePrintJobRequest.class), eq("student-123"))).thenReturn(response);

        mockMvc.perform(post("/api/print-jobs")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("job-1"))
            .andExpect(jsonPath("$.ownerId").value("student-123"));
    }

    @Test
    void getPrintJobsByStatus_StudentRole_Returns403Forbidden() throws Exception {
        User student = new User("student-123", "Student User", "student@university.edu", "+1234567890", Role.STUDENT, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(student);

        mockMvc.perform(get("/api/print-jobs/status/QUEUED")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getPrintJobsByStatus_OperatorRole_Returns200Ok() throws Exception {
        User operator = new User("op-123", "Operator User", "op@university.edu", "+1234567890", Role.OPERATOR, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(operator);

        when(printJobService.getPrintJobsByStatus(PrintJobStatus.QUEUED)).thenReturn(List.of());

        mockMvc.perform(get("/api/print-jobs/status/QUEUED")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void getPrintJobsByStatus_AdminRole_Returns200Ok() throws Exception {
        User admin = new User("admin-123", "Admin User", "admin@university.edu", "+1234567890", Role.ADMIN, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(admin);

        when(printJobService.getPrintJobsByStatus(PrintJobStatus.QUEUED)).thenReturn(List.of());

        mockMvc.perform(get("/api/print-jobs/status/QUEUED")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
