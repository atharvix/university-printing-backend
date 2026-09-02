package com.universityprinting.printing_backend.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "printers")
public class Printer {

    @Id
    private String id;

    private String name;

    private String location;

    @Indexed
    private PrinterStatus status = PrinterStatus.OFFLINE;

    private Set<ColorMode> supportedColorModes = new HashSet<>();

    private Set<PaperSize> supportedPaperSizes = new HashSet<>();

    private Boolean duplexSupported = false;

    @Indexed
    private String agentId;

    @Indexed
    private Boolean enabled = true;

    private Instant lastHeartbeatAt;

    private Instant createdAt;

    private Instant updatedAt;

    public Printer() {
        this.status = PrinterStatus.OFFLINE;
        this.enabled = true;
    }

    public Printer(
        String id,
        String name,
        String location,
        PrinterStatus status,
        Set<ColorMode> supportedColorModes,
        Set<PaperSize> supportedPaperSizes,
        Boolean duplexSupported,
        String agentId,
        Boolean enabled,
        Instant lastHeartbeatAt,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status != null ? status : PrinterStatus.OFFLINE;
        this.supportedColorModes = supportedColorModes != null ? supportedColorModes : new HashSet<>();
        this.supportedPaperSizes = supportedPaperSizes != null ? supportedPaperSizes : new HashSet<>();
        this.duplexSupported = duplexSupported != null ? duplexSupported : false;
        this.agentId = agentId;
        this.enabled = enabled != null ? enabled : true;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public PrinterStatus getStatus() {
        return status;
    }

    public void setStatus(PrinterStatus status) {
        this.status = status != null ? status : PrinterStatus.OFFLINE;
    }

    public Set<ColorMode> getSupportedColorModes() {
        return supportedColorModes;
    }

    public void setSupportedColorModes(Set<ColorMode> supportedColorModes) {
        this.supportedColorModes = supportedColorModes != null ? supportedColorModes : new HashSet<>();
    }

    public Set<PaperSize> getSupportedPaperSizes() {
        return supportedPaperSizes;
    }

    public void setSupportedPaperSizes(Set<PaperSize> supportedPaperSizes) {
        this.supportedPaperSizes = supportedPaperSizes != null ? supportedPaperSizes : new HashSet<>();
    }

    public Boolean getDuplexSupported() {
        return duplexSupported;
    }

    public void setDuplexSupported(Boolean duplexSupported) {
        this.duplexSupported = duplexSupported != null ? duplexSupported : false;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled != null ? enabled : true;
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
