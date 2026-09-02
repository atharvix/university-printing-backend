package com.universityprinting.printing_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.Url;
import com.universityprinting.printing_backend.exception.StorageException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class CloudinaryStorageServiceTest {

    private Cloudinary cloudinary;
    private Uploader uploader;
    private CloudinaryStorageService cloudinaryStorageService;

    @BeforeEach
    void setUp() {
        cloudinary = spy(new Cloudinary(Map.of(
            "cloud_name", "mock-cloud",
            "api_key", "mock-key",
            "api_secret", "mock-secret"
        )));
        uploader = mock(Uploader.class);
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryStorageService = new CloudinaryStorageService(cloudinary);
    }

    @Test
    void uploadFile_MissingApiKey_ThrowsStorageException() {
        Cloudinary unconfiguredCloudinary = new Cloudinary(Map.of());
        CloudinaryStorageService service = new CloudinaryStorageService(unconfiguredCloudinary);
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII));

        StorageException ex = assertThrows(
            StorageException.class,
            () -> service.uploadFile(file, "documents/u1/k1", "raw")
        );
        assertEquals("Cloudinary credentials not configured (missing CLOUDINARY_API_KEY).", ex.getMessage());
    }

    @Test
    void uploadFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII));
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of("public_id", "documents/u1/k1"));

        cloudinaryStorageService.uploadFile(file, "documents/u1/k1", "raw");

        verify(uploader).upload(eq(file.getBytes()), anyMap());
    }

    @Test
    void uploadFile_ThrowsException_TranslatesToStorageException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII));
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new RuntimeException("Cloudinary connection failed"));

        StorageException ex = assertThrows(
            StorageException.class,
            () -> cloudinaryStorageService.uploadFile(file, "documents/u1/k1", "raw")
        );
        assertEquals("Unexpected storage service error during file upload", ex.getMessage());
    }

    @Test
    void deleteFile_ThrowsException_TranslatesToStorageException() throws IOException {
        when(uploader.destroy(eq("documents/u1/k1"), anyMap())).thenThrow(new RuntimeException("Delete failed"));

        StorageException ex = assertThrows(
            StorageException.class,
            () -> cloudinaryStorageService.deleteFile("documents/u1/k1", "raw")
        );
        assertEquals("Failed to delete file from cloud storage", ex.getMessage());
    }

    @Test
    void generateDownloadUrl_Success() {
        Url mockUrl = mock(Url.class);
        when(cloudinary.url()).thenReturn(mockUrl);
        when(mockUrl.resourceType("raw")).thenReturn(mockUrl);
        when(mockUrl.secure(true)).thenReturn(mockUrl);
        when(mockUrl.generate("documents/u1/k1")).thenReturn("https://res.cloudinary.com/demo/raw/upload/documents/u1/k1");

        String url = cloudinaryStorageService.generateDownloadUrl("documents/u1/k1", "raw");
        assertEquals("https://res.cloudinary.com/demo/raw/upload/documents/u1/k1", url);
    }
}
