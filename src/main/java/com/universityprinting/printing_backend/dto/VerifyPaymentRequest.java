package com.universityprinting.printing_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyPaymentRequest {

    @NotBlank(message = "Provider order ID is required")
    private String providerOrderId;

    @NotBlank(message = "Provider payment ID is required")
    private String providerPaymentId;

    @NotBlank(message = "Provider signature is required")
    private String providerSignature;

    public VerifyPaymentRequest() {
    }

    public VerifyPaymentRequest(String providerOrderId, String providerPaymentId, String providerSignature) {
        this.providerOrderId = providerOrderId;
        this.providerPaymentId = providerPaymentId;
        this.providerSignature = providerSignature;
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
}
