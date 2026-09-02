package com.universityprinting.printing_backend.model;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "print_jobs")
public class PrintJob {

    @Id
    private String id;

    @Indexed
    private String ownerId;

    @Indexed
    private String documentId;

    private Integer copies;

    private ColorMode colorMode;

    private PaperSize paperSize;

    private Boolean duplex;

    private Integer pageCount;

    private BigDecimal price;

    private PrintJobStatus status = PrintJobStatus.QUEUED;

    private Instant createdAt;

    private Instant updatedAt;

    public PrintJob() {
        this.status = PrintJobStatus.QUEUED;
    }

    public PrintJob(
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
        this.id = id;
        this.ownerId = ownerId;
        this.documentId = documentId;
        this.copies = copies;
        this.colorMode = colorMode;
        this.paperSize = paperSize;
        this.duplex = duplex;
        this.pageCount = pageCount;
        this.price = price;
        this.status = status != null ? status : PrintJobStatus.QUEUED;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
        this.status = status != null ? status : PrintJobStatus.QUEUED;
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
