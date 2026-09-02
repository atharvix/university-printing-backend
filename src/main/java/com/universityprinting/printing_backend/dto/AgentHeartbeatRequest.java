package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.AgentStatus;
import com.universityprinting.printing_backend.model.PrinterStatus;
import java.util.Map;

public class AgentHeartbeatRequest {

    private AgentStatus status;
    private Map<String, PrinterStatus> printerStatuses;

    public AgentHeartbeatRequest() {
    }

    public AgentHeartbeatRequest(AgentStatus status, Map<String, PrinterStatus> printerStatuses) {
        this.status = status;
        this.printerStatuses = printerStatuses;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public Map<String, PrinterStatus> getPrinterStatuses() {
        return printerStatuses;
    }

    public void setPrinterStatuses(Map<String, PrinterStatus> printerStatuses) {
        this.printerStatuses = printerStatuses;
    }
}
