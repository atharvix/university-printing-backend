package com.universityprinting.printing_backend.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "print_agents")
public class PrintAgent {

    @Id
    private String id;

    private String name;

    private String hostName;

    @Indexed(unique = true)
    private String apiKeyHash;

    @Indexed
    private AgentStatus status = AgentStatus.ACTIVE;

    private Set<String> assignedPrinterIds = new HashSet<>();

    private Instant lastHeartbeatAt;

    private Instant createdAt;

    private Instant updatedAt;

    public PrintAgent() {
        this.status = AgentStatus.ACTIVE;
    }

    public PrintAgent(
        String id,
        String name,
        String hostName,
        String apiKeyHash,
        AgentStatus status,
        Set<String> assignedPrinterIds,
        Instant lastHeartbeatAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.hostName = hostName;
        this.apiKeyHash = apiKeyHash;
        this.status = status != null ? status : AgentStatus.ACTIVE;
        this.assignedPrinterIds = assignedPrinterIds != null ? assignedPrinterIds : new HashSet<>();
        this.lastHeartbeatAt = lastHeartbeatAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status != null ? status : AgentStatus.ACTIVE;
    }

    public Set<String> getAssignedPrinterIds() {
        return assignedPrinterIds;
    }

    public void setAssignedPrinterIds(Set<String> assignedPrinterIds) {
        this.assignedPrinterIds = assignedPrinterIds != null ? assignedPrinterIds : new HashSet<>();
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
