package com.universityprinting.printing_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    void uploadFile(MultipartFile file, String storageKey, String resourceType);

    void deleteFile(String storageKey, String resourceType);

    String generateDownloadUrl(String storageKey, String resourceType);
}
