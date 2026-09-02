package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class CreatePrinterRequest {

    @NotBlank(message = "Printer name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotEmpty(message = "At least one supported color mode is required")
    private Set<ColorMode> supportedColorModes;

    @NotEmpty(message = "At least one supported paper size is required")
    private Set<PaperSize> supportedPaperSizes;

    private Boolean duplexSupported = false;

    private String agentId;

    public CreatePrinterRequest() {
    }

    public CreatePrinterRequest(
        String name,
        String location,
        Set<ColorMode> supportedColorModes,
        Set<PaperSize> supportedPaperSizes,
        Boolean duplexSupported,
        String agentId
    ) {
        this.name = name;
        this.location = location;
        this.supportedColorModes = supportedColorModes;
        this.supportedPaperSizes = supportedPaperSizes;
        this.duplexSupported = duplexSupported != null ? duplexSupported : false;
        this.agentId = agentId;
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
        this.duplexSupported = duplexSupported != null ? duplexSupported : false;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
