package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJob;
import com.universityprinting.printing_backend.model.PrintJobStatus;
import java.math.BigDecimal;
import java.time.Instant;

public class PrintJobResponse {

    private String id;
    private String ownerId;
    private String documentId;
    private Integer copies;
    private ColorMode colorMode;
    private PaperSize paperSize;
    private Boolean duplex;
    private Integer pageCount;
    private BigDecimal price;
    private PrintJobStatus status;
    private Boolean queueEligible;
    private String assignedPrinterId;
    private String assignedAgentId;
    private Instant createdAt;
    private Instant updatedAt;

    public PrintJobResponse() {
    }

    public PrintJobResponse(
        String id,
        String ownerId,
        String documentId,
        Integer copies,
        ColorMode colorMode,
        PaperSize paperSize,
        Boolean duplex,
        Integer pageCount,
        BigDecimal price,
        PrintJobStatus status,
        Boolean queueEligible,
        String assignedPrinterId,
        String assignedAgentId,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.documentId = documentId;
        this.copies = copies;
        this.colorMode = colorMode;
        this.paperSize = paperSize;
        this.duplex = duplex;
        this.pageCount = pageCount;
        this.price = price;
        this.status = status;
        this.queueEligible = queueEligible;
        this.assignedPrinterId = assignedPrinterId;
        this.assignedAgentId = assignedAgentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PrintJobResponse(
        String id,
        String ownerId,
        String documentId,
        Integer copies,
        ColorMode colorMode,
        PaperSize paperSize,
        Boolean duplex,
        Integer pageCount,
        BigDecimal price,
        PrintJobStatus status,
        Instant createdAt,
        Instant updatedAt
    ) {
        this(id, ownerId, documentId, copies, colorMode, paperSize, duplex, pageCount, price, status, false, null, null, createdAt, updatedAt);
    }

    public static PrintJobResponse from(PrintJob job) {
        return new PrintJobResponse(
            job.getId(),
            job.getOwnerId(),
            job.getDocumentId(),
            job.getCopies(),
            job.getColorMode(),
            job.getPaperSize(),
            job.getDuplex(),
            job.getPageCount(),
            job.getPrice(),
            job.getStatus(),
            job.getQueueEligible(),
            job.getAssignedPrinterId(),
            job.getAssignedAgentId(),
            job.getCreatedAt(),
            job.getUpdatedAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public Boolean getDuplex() {
        return duplex;
    }

    public void setDuplex(Boolean duplex) {
        this.duplex = duplex;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PrintJobStatus getStatus() {
        return status;
    }

    public void setStatus(PrintJobStatus status) {
        this.status = status;
    }

    public Boolean getQueueEligible() {
        return queueEligible;
    }

    public void setQueueEligible(Boolean queueEligible) {
        this.queueEligible = queueEligible;
    }

    public String getAssignedPrinterId() {
        return assignedPrinterId;
    }

    public void setAssignedPrinterId(String assignedPrinterId) {
        this.assignedPrinterId = assignedPrinterId;
    }

    public String getAssignedAgentId() {
        return assignedAgentId;
    }

    public void setAssignedAgentId(String assignedAgentId) {
        this.assignedAgentId = assignedAgentId;
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
