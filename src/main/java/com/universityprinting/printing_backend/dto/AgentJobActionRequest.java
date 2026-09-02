package com.universityprinting.printing_backend.dto;

public class AgentJobActionRequest {

    private String reason;

    public AgentJobActionRequest() {
    }

    public AgentJobActionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
