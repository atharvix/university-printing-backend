package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrinterStatus;
import java.util.Set;

public class UpdatePrinterRequest {

    private String name;
    private String location;
    private PrinterStatus status;
    private Set<ColorMode> supportedColorModes;
    private Set<PaperSize> supportedPaperSizes;
    private Boolean duplexSupported;
    private String agentId;
    private Boolean enabled;

    public UpdatePrinterRequest() {
    }

    public UpdatePrinterRequest(
        String name,
        String location,
        PrinterStatus status,
        Set<ColorMode> supportedColorModes,
        Set<PaperSize> supportedPaperSizes,
        Boolean duplexSupported,
        String agentId,
        Boolean enabled
    ) {
        this.name = name;
        this.location = location;
        this.status = status;
        this.supportedColorModes = supportedColorModes;
        this.supportedPaperSizes = supportedPaperSizes;
        this.duplexSupported = duplexSupported;
        this.agentId = agentId;
        this.enabled = enabled;
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
}
