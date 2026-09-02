package com.universityprinting.printing_backend.dto;

public record DocumentDownloadResponse(
    String id,
    String originalFileName,
    String downloadUrl
) {
}
