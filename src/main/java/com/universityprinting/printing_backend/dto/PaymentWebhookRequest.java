package com.universityprinting.printing_backend.dto;

public class PaymentWebhookRequest {

    private String event;
    private String providerOrderId;
    private String providerPaymentId;
    private String status;

    public PaymentWebhookRequest() {
    }

    public PaymentWebhookRequest(String event, String providerOrderId, String providerPaymentId, String status) {
        this.event = event;
        this.providerOrderId = providerOrderId;
        this.providerPaymentId = providerPaymentId;
        this.status = status;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
