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
import com.universityprinting.printing_backend.dto.CreatePaymentRequest;
import com.universityprinting.printing_backend.dto.PaymentResponse;
import com.universityprinting.printing_backend.dto.PaymentWebhookRequest;
import com.universityprinting.printing_backend.model.PaymentStatus;
import com.universityprinting.printing_backend.model.Role;
import com.universityprinting.printing_backend.model.User;
import com.universityprinting.printing_backend.repository.DocumentRepository;
import com.universityprinting.printing_backend.repository.PaymentRepository;
import com.universityprinting.printing_backend.repository.PrintJobRepository;
import com.universityprinting.printing_backend.repository.UserRepository;
import com.universityprinting.printing_backend.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
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
class PaymentSecurityIntegrationTest {

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
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PaymentService paymentService;

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
    void createPayment_Unauthenticated_Returns401Unauthorized() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest("job-1");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void getMyPayments_Unauthenticated_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/payments"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void createPayment_AuthenticatedStudent_Returns201Created() throws Exception {
        User user = new User("student-123", "Student User", "student@university.edu", "+1234567890", Role.STUDENT, "hash", Instant.now(), Instant.now());
        String token = jwtService.generateToken(user);

        CreatePaymentRequest request = new CreatePaymentRequest("job-1");
        PaymentResponse response = new PaymentResponse("pay-1", "job-1", "student-123", new BigDecimal("25.00"), PaymentStatus.CREATED, "MOCK", "order_123", null, Instant.now(), Instant.now());

        when(paymentService.createPayment(any(CreatePaymentRequest.class), eq("student-123"))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("pay-1"))
            .andExpect(jsonPath("$.ownerId").value("student-123"));
    }

    @Test
    void webhook_Unauthenticated_Allowed() throws Exception {
        PaymentWebhookRequest request = new PaymentWebhookRequest("payment.captured", "order_123", "pay_456", "PAID");

        mockMvc.perform(post("/api/payments/webhook")
                .header("X-Webhook-Signature", "mock-sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }
}
