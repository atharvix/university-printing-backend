package com.universityprinting.printing_backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public class RegisterAgentRequest {

    @NotBlank(message = "Agent name is required")
    private String name;

    private String hostName;

    private Set<String> assignedPrinterIds;

    public RegisterAgentRequest() {
    }

    public RegisterAgentRequest(String name, String hostName, Set<String> assignedPrinterIds) {
        this.name = name;
        this.hostName = hostName;
        this.assignedPrinterIds = assignedPrinterIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Set<String> getAssignedPrinterIds() {
        return assignedPrinterIds;
    }

    public void setAssignedPrinterIds(Set<String> assignedPrinterIds) {
        this.assignedPrinterIds = assignedPrinterIds;
    }
}
