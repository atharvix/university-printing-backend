package com.universityprinting.printing_backend.dto;

import com.universityprinting.printing_backend.model.Document;
import java.time.Instant;

public record DocumentResponse(
    String id,
    String ownerId,
    String originalFileName,
    String contentType,
    long fileSize,
    Instant createdAt,
    Instant updatedAt
) {
    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
            document.getId(),
            document.getOwnerId(),
            document.getOriginalFileName(),
            document.getContentType(),
            document.getFileSize(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }
}
