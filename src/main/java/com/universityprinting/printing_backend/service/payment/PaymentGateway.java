package com.universityprinting.printing_backend.service.payment;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentOrderResult createOrder(String printJobId, BigDecimal amount, String currency);

    PaymentVerificationResult verifyPayment(String providerOrderId, String providerPaymentId, String signature);

    boolean verifyWebhookSignature(String rawPayload, String signatureHeader);

    String getProviderName();
}
