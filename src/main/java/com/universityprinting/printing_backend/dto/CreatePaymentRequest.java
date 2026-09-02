package com.universityprinting.printing_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePaymentRequest {

    @NotBlank(message = "Print job ID is required")
    private String printJobId;

    public CreatePaymentRequest() {
    }

    public CreatePaymentRequest(String printJobId) {
        this.printJobId = printJobId;
    }

    public String getPrintJobId() {
        return printJobId;
    }

    public void setPrintJobId(String printJobId) {
        this.printJobId = printJobId;
    }
}
