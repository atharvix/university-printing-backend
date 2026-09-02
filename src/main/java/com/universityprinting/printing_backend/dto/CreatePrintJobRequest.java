package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.ColorMode;
import com.universityprinting.printing_backend.model.PaperSize;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreatePrintJobRequest {

    @NotBlank(message = "Document ID is required")
    private String documentId;

    @NotNull(message = "Copies is required")
    @Min(value = 1, message = "Copies must be at least 1")
    private Integer copies;

    @NotNull(message = "Color mode is required")
    private ColorMode colorMode;

    @NotNull(message = "Paper size is required")
    private PaperSize paperSize;

    @NotNull(message = "Duplex option is required")
    private Boolean duplex;

    @NotNull(message = "Page count is required")
    @Min(value = 1, message = "Page count must be at least 1")
    private Integer pageCount;

    public CreatePrintJobRequest() {
    }

    public CreatePrintJobRequest(
        String documentId,
        Integer copies,
        ColorMode colorMode,
        PaperSize paperSize,
        Boolean duplex,
        Integer pageCount
    ) {
        this.documentId = documentId;
        this.copies = copies;
        this.colorMode = colorMode;
        this.paperSize = paperSize;
        this.duplex = duplex;
        this.pageCount = pageCount;
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
}
