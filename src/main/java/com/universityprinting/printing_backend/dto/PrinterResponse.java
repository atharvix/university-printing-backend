package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.Printer;
import com.universityprinting.printing_backend.model.PrinterStatus;
import java.time.Instant;
import java.util.Set;

public class PrinterResponse {

    private String id;
    private String name;
    private String location;
    private PrinterStatus status;
    private Set<ColorMode> supportedColorModes;
    private Set<PaperSize> supportedPaperSizes;
    private Boolean duplexSupported;
    private String agentId;
    private Boolean enabled;
    private Instant lastHeartbeatAt;
    private Instant createdAt;
    private Instant updatedAt;

    public PrinterResponse() {
    }

    public PrinterResponse(
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
        this.status = status;
        this.supportedColorModes = supportedColorModes;
        this.supportedPaperSizes = supportedPaperSizes;
        this.duplexSupported = duplexSupported;
        this.agentId = agentId;
        this.enabled = enabled;
        this.lastHeartbeatAt = lastHeartbeatAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PrinterResponse from(Printer printer) {
        return new PrinterResponse(
            printer.getId(),
            printer.getName(),
            printer.getLocation(),
            printer.getStatus(),
            printer.getSupportedColorModes(),
            printer.getSupportedPaperSizes(),
            printer.getDuplexSupported(),
            printer.getAgentId(),
            printer.getEnabled(),
            printer.getLastHeartbeatAt(),
            printer.getCreatedAt(),
            printer.getUpdatedAt()
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
        this.status = status;
    }

    public Set<ColorMode> getSupportedColorModes() {
        return supportedColorModes;
    }

    public void setSupportedColorModes(Set<ColorMode> supportedColorModes) {
        this.supportedColorModes = supportedColorModes;
    }

    public Set<PaperSize> getSupportedPaperSizes() {
        return supportedPaperSizes;
    }

    public void setSupportedPaperSizes(Set<PaperSize> supportedPaperSizes) {
        this.supportedPaperSizes = supportedPaperSizes;
    }

    public Boolean getDuplexSupported() {
        return duplexSupported;
    }

    public void setDuplexSupported(Boolean duplexSupported) {
        this.duplexSupported = duplexSupported;
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
        this.enabled = enabled;
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
