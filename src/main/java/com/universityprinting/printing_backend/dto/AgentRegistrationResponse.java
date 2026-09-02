package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.PrintAgent;

public class AgentRegistrationResponse {

    private String id;
    private String name;
    private String rawApiKey;
    private AgentStatus status;

    public AgentRegistrationResponse() {
    }

    public AgentRegistrationResponse(String id, String name, String rawApiKey, AgentStatus status) {
        this.id = id;
        this.name = name;
        this.rawApiKey = rawApiKey;
        this.status = status;
    }

    public static AgentRegistrationResponse of(PrintAgent agent, String rawApiKey) {
        return new AgentRegistrationResponse(
            agent.getId(),
            agent.getName(),
            rawApiKey,
            agent.getStatus()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRawApiKey() {
        return rawApiKey;
    }

    public void setRawApiKey(String rawApiKey) {
        this.rawApiKey = rawApiKey;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }
}
