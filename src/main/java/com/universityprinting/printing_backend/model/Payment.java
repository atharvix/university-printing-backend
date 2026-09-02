package com.universityprinting.printing_backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private String printJobId;

    @Indexed
    private String ownerId;

    private BigDecimal amount;

    private PaymentStatus status;

    private String provider;

    @Indexed
    private String providerOrderId;

    private String providerPaymentId;

    private String providerSignature;

    private Instant createdAt;

    private Instant updatedAt;

    public Payment() {
        this.status = PaymentStatus.CREATED;
    }

    public Payment(
        String id,
        String printJobId,
        String ownerId,
        BigDecimal amount,
        PaymentStatus status,
        String provider,
        String providerOrderId,
        String providerPaymentId,
        String providerSignature,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.printJobId = printJobId;
        this.ownerId = ownerId;
        this.amount = amount;
        this.status = status != null ? status : PaymentStatus.CREATED;
        this.provider = provider;
        this.providerOrderId = providerOrderId;
        this.providerPaymentId = providerPaymentId;
        this.providerSignature = providerSignature;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
        this.status = status != null ? status : PaymentStatus.CREATED;
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

    public String getProviderSignature() {
        return providerSignature;
    }

    public void setProviderSignature(String providerSignature) {
        this.providerSignature = providerSignature;
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
