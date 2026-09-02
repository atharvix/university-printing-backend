package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.Payment;
import com.universityprinting.printing_backend.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class PaymentResponse {

    private String id;
    private String printJobId;
    private String ownerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String provider;
    private String providerOrderId;
    private String providerPaymentId;
    private Instant createdAt;
    private Instant updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(
        String id,
        String printJobId,
        String ownerId,
        BigDecimal amount,
        PaymentStatus status,
        String provider,
        String providerOrderId,
        String providerPaymentId,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.printJobId = printJobId;
        this.ownerId = ownerId;
        this.amount = amount;
        this.status = status;
        this.provider = provider;
        this.providerOrderId = providerOrderId;
        this.providerPaymentId = providerPaymentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getPrintJobId(),
            payment.getOwnerId(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getProvider(),
            payment.getProviderOrderId(),
            payment.getProviderPaymentId(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrintJobId() {
        return printJobId;
    }

    public void setPrintJobId(String printJobId) {
        this.printJobId = printJobId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderOrderId() {
        return providerOrderId;
    }

    public void setProviderOrderId(String providerOrderId) {
        this.providerOrderId = providerOrderId;
    }

    public String getProviderPaymentId() {
        return providerPaymentId;
    }

    public void setProviderPaymentId(String providerPaymentId) {
        this.providerPaymentId = providerPaymentId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
