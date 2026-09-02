package com.universityprinting.printing_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.universityprinting.printing_backend.exception.StorageException;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);
    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public void uploadFile(MultipartFile file, String storageKey, String resourceType) {
        if (cloudinary.config == null || cloudinary.config.apiKey == null || cloudinary.config.apiKey.trim().isEmpty()) {
            log.error("Cloudinary upload aborted for storage key {}: missing CLOUDINARY_API_KEY configuration.", storageKey);
            throw new StorageException("Cloudinary credentials not configured (missing CLOUDINARY_API_KEY).");
        }
        try {
            Map<?, ?> options = ObjectUtils.asMap(
                "public_id", storageKey,
                "resource_type", resourceType,
                "overwrite", true
            );
            cloudinary.uploader().upload(file.getBytes(), options);
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary for storage key: {}. Cause: {}", storageKey, e.getMessage(), e);
            throw new StorageException("Failed to upload file to cloud storage", e);
        } catch (Exception e) {
            log.error("Unexpected error during Cloudinary upload for storage key: {}. Error type: {}, Message: {}", storageKey, e.getClass().getName(), e.getMessage(), e);
            throw new StorageException("Unexpected storage service error during file upload", e);
        }
    }

    @Override
    public void deleteFile(String storageKey, String resourceType) {
        try {
            Map<?, ?> options = ObjectUtils.asMap(
                "resource_type", resourceType
            );
            cloudinary.uploader().destroy(storageKey, options);
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary for storage key: {}. Error type: {}, Message: {}", storageKey, e.getClass().getName(), e.getMessage(), e);
            throw new StorageException("Failed to delete file from cloud storage", e);
        }
    }

    @Override
    public String generateDownloadUrl(String storageKey, String resourceType) {
        try {
            return cloudinary.url()
                .resourceType(resourceType)
                .secure(true)
                .generate(storageKey);
        } catch (Exception e) {
            log.error("Failed to generate download URL for storage key: {}. Error type: {}, Message: {}", storageKey, e.getClass().getName(), e.getMessage(), e);
            throw new StorageException("Failed to generate secure document download URL", e);
        }
    }
}
