package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import com.universityprinting.printing_backend.model.PrintJob;

public class AgentJobResponse {

    private String id;
    private String documentId;
    private String documentDownloadUrl;
    private Integer copies;
    private ColorMode colorMode;
    private PaperSize paperSize;
    private Boolean duplex;
    private Integer pageCount;
    private String assignedPrinterId;

    public AgentJobResponse() {
    }

    public AgentJobResponse(
        String id,
        String documentId,
        String documentDownloadUrl,
        Integer copies,
        ColorMode colorMode,
        PaperSize paperSize,
        Boolean duplex,
        Integer pageCount,
        String assignedPrinterId
    ) {
        this.id = id;
        this.documentId = documentId;
        this.documentDownloadUrl = documentDownloadUrl;
        this.copies = copies;
        this.colorMode = colorMode;
        this.paperSize = paperSize;
        this.duplex = duplex;
        this.pageCount = pageCount;
        this.assignedPrinterId = assignedPrinterId;
    }

    public static AgentJobResponse from(PrintJob job, String downloadUrl) {
        return new AgentJobResponse(
            job.getId(),
            job.getDocumentId(),
            downloadUrl,
            job.getCopies(),
            job.getColorMode(),
            job.getPaperSize(),
            job.getDuplex(),
            job.getPageCount(),
            job.getAssignedPrinterId()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocumentDownloadUrl() {
        return documentDownloadUrl;
    }

    public void setDocumentDownloadUrl(String documentDownloadUrl) {
        this.documentDownloadUrl = documentDownloadUrl;
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

    public String getAssignedPrinterId() {
        return assignedPrinterId;
    }

    public void setAssignedPrinterId(String assignedPrinterId) {
        this.assignedPrinterId = assignedPrinterId;
    }
}
