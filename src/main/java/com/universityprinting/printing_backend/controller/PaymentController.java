package com.universityprinting.printing_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universityprinting.printing_backend.dto.CreatePaymentRequest;
import com.universityprinting.printing_backend.dto.PaymentResponse;
import com.universityprinting.printing_backend.dto.PaymentWebhookRequest;
import com.universityprinting.printing_backend.dto.VerifyPaymentRequest;
import com.universityprinting.printing_backend.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
        @Valid @RequestBody CreatePaymentRequest request,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PaymentResponse response = paymentService.createPayment(request, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        List<PaymentResponse> response = paymentService.getPaymentsByOwner(ownerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(
        @PathVariable("id") String id,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PaymentResponse response = paymentService.getPaymentByIdAndOwner(id, ownerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
        @PathVariable("id") String id,
        @Valid @RequestBody VerifyPaymentRequest request,
        @AuthenticationPrincipal Jwt jwt,
        Authentication authentication
    ) {
        String ownerId = extractUserId(jwt, authentication);
        PaymentResponse response = paymentService.verifyPayment(id, request, ownerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
        @RequestHeader(value = "X-Webhook-Signature", required = false) String signatureHeader,
        @RequestBody String rawPayload
    ) {
        try {
            PaymentWebhookRequest webhookRequest = objectMapper.readValue(rawPayload, PaymentWebhookRequest.class);
            paymentService.handleWebhook(rawPayload, signatureHeader, webhookRequest);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Failed to process webhook payload", e);
        }
    }

    private String extractUserId(Jwt jwt, Authentication authentication) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }
        if (authentication != null && authentication.getName() != null) {
            return authentication.getName();
        }
        throw new AccessDeniedException("Authentication principal is missing");
    }
}
