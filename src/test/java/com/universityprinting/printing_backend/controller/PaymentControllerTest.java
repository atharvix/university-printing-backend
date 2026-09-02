package com.universityprinting.printing_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.dto.CreatePaymentRequest;
import com.universityprinting.printing_backend.dto.PaymentResponse;
import com.universityprinting.printing_backend.dto.PaymentWebhookRequest;
import com.universityprinting.printing_backend.dto.VerifyPaymentRequest;
import com.universityprinting.printing_backend.exception.GlobalExceptionHandler;
import com.universityprinting.printing_backend.exception.PaymentNotFoundException;
import com.universityprinting.printing_backend.exception.PaymentVerificationException;
import com.universityprinting.printing_backend.model.PaymentStatus;
import com.universityprinting.printing_backend.service.PaymentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private MockMvc mockMvc;
    private Jwt mockJwt;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PaymentController paymentController = new PaymentController(paymentService);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                    return parameter.getParameterType().isAssignableFrom(Jwt.class);
                }

                @Override
                public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                              org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                              org.springframework.web.context.request.NativeWebRequest webRequest,
                                              org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                    return mockJwt;
                }
            })
            .build();

        mockJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "HS256")
            .subject("student-123")
            .claim("email", "student@university.edu")
            .claim("role", "STUDENT")
            .build();
    }

    @Test
    void createPayment_Success_Returns201Created() throws Exception {
        CreatePaymentRequest request = new CreatePaymentRequest("job-1");
        PaymentResponse response = new PaymentResponse("pay-1", "job-1", "student-123", new BigDecimal("20.00"), PaymentStatus.CREATED, "MOCK", "order_123", null, Instant.now(), Instant.now());

        when(paymentService.createPayment(any(CreatePaymentRequest.class), eq("student-123"))).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("pay-1"))
            .andExpect(jsonPath("$.printJobId").value("job-1"))
            .andExpect(jsonPath("$.amount").value(20.00))
            .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void createPayment_ValidationFailure_Returns400() throws Exception {
        CreatePaymentRequest invalidRequest = new CreatePaymentRequest("");

        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Validation failed"))
            .andExpect(jsonPath("$.details.printJobId").exists());
    }

    @Test
    void getMyPayments_Returns200Ok() throws Exception {
        PaymentResponse response = new PaymentResponse("pay-1", "job-1", "student-123", new BigDecimal("20.00"), PaymentStatus.PAID, "MOCK", "order_123", "pay_456", Instant.now(), Instant.now());
        when(paymentService.getPaymentsByOwner("student-123")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("pay-1"));
    }

    @Test
    void getPaymentById_Success_Returns200Ok() throws Exception {
        PaymentResponse response = new PaymentResponse("pay-1", "job-1", "student-123", new BigDecimal("20.00"), PaymentStatus.PAID, "MOCK", "order_123", "pay_456", Instant.now(), Instant.now());
        when(paymentService.getPaymentByIdAndOwner("pay-1", "student-123")).thenReturn(response);

        mockMvc.perform(get("/api/payments/pay-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("pay-1"));
    }

    @Test
    void getPaymentById_NotFound_Returns404() throws Exception {
        when(paymentService.getPaymentByIdAndOwner("pay-unknown", "student-123"))
            .thenThrow(new PaymentNotFoundException("Payment not found with ID: pay-unknown"));

        mockMvc.perform(get("/api/payments/pay-unknown"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not found"));
    }

    @Test
    void verifyPayment_Success_Returns200Ok() throws Exception {
        VerifyPaymentRequest request = new VerifyPaymentRequest("order_123", "pay_456", "sig_valid");
        PaymentResponse response = new PaymentResponse("pay-1", "job-1", "student-123", new BigDecimal("20.00"), PaymentStatus.PAID, "MOCK", "order_123", "pay_456", Instant.now(), Instant.now());

        when(paymentService.verifyPayment(eq("pay-1"), any(VerifyPaymentRequest.class), eq("student-123"))).thenReturn(response);

        mockMvc.perform(post("/api/payments/pay-1/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("pay-1"))
            .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void handleWebhook_Success_Returns200Ok() throws Exception {
        PaymentWebhookRequest webhookRequest = new PaymentWebhookRequest("payment.captured", "order_123", "pay_456", "PAID");
        String rawPayload = objectMapper.writeValueAsString(webhookRequest);

        doNothing().when(paymentService).handleWebhook(eq(rawPayload), eq("sig_valid"), any(PaymentWebhookRequest.class));

        mockMvc.perform(post("/api/payments/webhook")
                .header("X-Webhook-Signature", "sig_valid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rawPayload))
            .andExpect(status().isOk());
    }
}
