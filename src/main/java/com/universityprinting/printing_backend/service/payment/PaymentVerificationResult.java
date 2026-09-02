package com.universityprinting.printing_backend.service.payment;

public record PaymentVerificationResult(
    boolean verified,
    String providerPaymentId,
    String message
) {}
