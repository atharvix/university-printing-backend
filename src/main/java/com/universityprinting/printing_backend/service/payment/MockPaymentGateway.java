package com.universityprinting.printing_backend.service.payment;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);
    private final String secretKey;

    public MockPaymentGateway(@Value("${payment.mock.secret:mock_payment_secret_key_12345}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public PaymentOrderResult createOrder(String printJobId, BigDecimal amount, String currency) {
        String providerOrderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("[PAYMENT GATEWAY] Created mock order {} for job {} with amount {} {}", providerOrderId, printJobId, amount, currency);
        return new PaymentOrderResult(providerOrderId, amount, currency, getProviderName());
    }

    @Override
    public PaymentVerificationResult verifyPayment(String providerOrderId, String providerPaymentId, String signature) {
        if (providerOrderId == null || providerPaymentId == null || signature == null) {
            return new PaymentVerificationResult(false, providerPaymentId, "Missing required verification parameters");
        }

        String expectedSignature = generateSignature(providerOrderId + "|" + providerPaymentId);
        boolean matches = MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            signature.getBytes(StandardCharsets.UTF_8)
        );

        if (matches) {
            log.info("[PAYMENT GATEWAY] Verified payment for order {}", providerOrderId);
            return new PaymentVerificationResult(true, providerPaymentId, "Payment verified successfully");
        } else {
            log.warn("[PAYMENT GATEWAY] Signature mismatch for order {}", providerOrderId);
            return new PaymentVerificationResult(false, providerPaymentId, "Invalid signature");
        }
    }

    @Override
    public boolean verifyWebhookSignature(String rawPayload, String signatureHeader) {
        if (rawPayload == null || signatureHeader == null) {
            return false;
        }
        String expectedSignature = generateSignature(rawPayload);
        return MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            signatureHeader.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String getProviderName() {
        return "MOCK_GATEWAY";
    }

    public String generateSignature(String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }
}
