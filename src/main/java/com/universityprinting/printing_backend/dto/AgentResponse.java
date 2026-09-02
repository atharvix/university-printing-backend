package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.PrintAgent;
import java.time.Instant;
import java.util.Set;

public class AgentResponse {

    private String id;
    private String name;
    private String hostName;
    private AgentStatus status;
    private Set<String> assignedPrinterIds;
    private Instant lastHeartbeatAt;
    private Instant createdAt;
    private Instant updatedAt;

    public AgentResponse() {
    }

    public AgentResponse(
        String id,
        String name,
        String hostName,
        AgentStatus status,
        Set<String> assignedPrinterIds,
        Instant lastHeartbeatAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.hostName = hostName;
        this.status = status;
        this.assignedPrinterIds = assignedPrinterIds;
        this.lastHeartbeatAt = lastHeartbeatAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AgentResponse from(PrintAgent agent) {
        return new AgentResponse(
            agent.getId(),
            agent.getName(),
            agent.getHostName(),
            agent.getStatus(),
            agent.getAssignedPrinterIds(),
            agent.getLastHeartbeatAt(),
            agent.getCreatedAt(),
            agent.getUpdatedAt()
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public Set<String> getAssignedPrinterIds() {
        return assignedPrinterIds;
    }

    public void setAssignedPrinterIds(Set<String> assignedPrinterIds) {
        this.assignedPrinterIds = assignedPrinterIds;
    }

    public Instant getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(Instant lastHeartbeatAt) {
        this.lastHeartbeatAt = lastHeartbeatAt;
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
