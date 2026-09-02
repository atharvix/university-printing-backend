package com.universityprinting.printing_backend.config;

import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final String cloudinaryUrl;

    public CloudinaryConfig(
        @Value("${cloudinary.cloud-name:}") String cloudName,
        @Value("${cloudinary.api-key:}") String apiKey,
        @Value("${cloudinary.api-secret:}") String apiSecret,
        @Value("${cloudinary.url:}") String cloudinaryUrl
    ) {
        this.cloudName = cloudName != null ? cloudName.trim() : "";
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiSecret = apiSecret != null ? apiSecret.trim() : "";
        this.cloudinaryUrl = cloudinaryUrl != null ? cloudinaryUrl.trim() : "";
    }

    @Bean
    public Cloudinary cloudinary() {
        if (!cloudinaryUrl.isEmpty()) {
            log.info("[CLOUDINARY] Configured via CLOUDINARY_URL environment variable.");
            return new Cloudinary(cloudinaryUrl);
        }

        boolean hasCloudName = !cloudName.isEmpty();
        boolean hasApiKey = !apiKey.isEmpty();
        boolean hasApiSecret = !apiSecret.isEmpty();

        if (hasCloudName && hasApiKey && hasApiSecret) {
            log.info("[CLOUDINARY] Configured with cloud_name, api_key, and api_secret environment variables.");
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            config.put("secure", true);
            return new Cloudinary(config);
        }

        log.warn("[CLOUDINARY] Required configuration is missing! Status: CLOUDINARY_CLOUD_NAME present={}, CLOUDINARY_API_KEY present={}, CLOUDINARY_API_SECRET present={}. Uploads will fail until these environment variables are set.",
            hasCloudName, hasApiKey, hasApiSecret);

        Map<String, Object> fallbackConfig = new HashMap<>();
        if (hasCloudName) fallbackConfig.put("cloud_name", cloudName);
        if (hasApiKey) fallbackConfig.put("api_key", apiKey);
        if (hasApiSecret) fallbackConfig.put("api_secret", apiSecret);
        fallbackConfig.put("secure", true);
        return new Cloudinary(fallbackConfig);
    }
}
