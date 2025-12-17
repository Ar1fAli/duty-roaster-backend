package com.infotech.config;

import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class EnvConfig {

  private static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);

  // Database
  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Value("${spring.datasource.username}")
  private String dbUsername;

  // Firebase will be read directly from env vars
  private String firebaseConfig;

  // Cloudinary
  @Value("${cloudinary.cloud-name}")
  private String cloudinaryCloudName;

  @Value("${cloudinary.api-key}")
  private String cloudinaryApiKey;

  @PostConstruct
  public void init() {
    logger.info("==========================================");
    logger.info("🚀 APPLICATION ENVIRONMENT CONFIGURATION");
    logger.info("==========================================");

    // Load Firebase configuration
    loadFirebaseConfig();

    // Log all configurations (masking sensitive data)
    logConfigurations();

    // Validate configurations
    validateConfigurations();

    logger.info("==========================================");
  }

  private void loadFirebaseConfig() {
    // Priority 1: Base64 encoded environment variable
    String base64Config = System.getenv("FIREBASE_CONFIG_BASE64");
    if (base64Config != null && !base64Config.trim().isEmpty()) {
      try {
        byte[] decodedBytes = Base64.getDecoder().decode(base64Config);
        firebaseConfig = new String(decodedBytes);
        logger.info("✓ Firebase config loaded from FIREBASE_CONFIG_BASE64");
      } catch (Exception e) {
        logger.error("Failed to decode Base64 Firebase config: {}", e.getMessage());
      }
    }

    // Priority 2: JSON string environment variable
    if (firebaseConfig == null) {
      String jsonConfig = System.getenv("FIREBASE_CONFIG_JSON");
      if (jsonConfig != null && !jsonConfig.trim().isEmpty()) {
        firebaseConfig = jsonConfig;
        logger.info("✓ Firebase config loaded from FIREBASE_CONFIG_JSON");
      }
    }

    // Priority 3: Local file (development only)
    if (firebaseConfig == null) {
      try {
        ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
        if (resource.exists()) {
          try (InputStream is = resource.getInputStream()) {
            firebaseConfig = new String(is.readAllBytes());
            logger.info("✓ Firebase config loaded from local file");
          }
        }
      } catch (Exception e) {
        logger.warn("No local Firebase config file found");
      }
    }
  }

  private void logConfigurations() {
    // Database
    String maskedDbUrl = maskSensitiveData(dbUrl, "password");
    logger.info("📦 DATABASE:");
    logger.info("  URL: {}", maskedDbUrl);
    logger.info("  Username: {}", maskValue(dbUsername));

    // Firebase
    if (firebaseConfig != null && !firebaseConfig.isEmpty()) {
      logger.info("🔥 FIREBASE:");
      logger.info("  Configured: ✓ ({} bytes)", firebaseConfig.length());
      logger.info("  Project: {}", extractFirebaseProject(firebaseConfig));
    } else {
      logger.info("🔥 FIREBASE: ✗ NOT CONFIGURED");
    }

    // Cloudinary
    logger.info("☁️  CLOUDINARY:");
    logger.info("  Cloud Name: {}", maskValue(cloudinaryCloudName));
    logger.info("  API Key: {}", maskValue(cloudinaryApiKey, 4));
    logger.info("  Configured: {}",
        (cloudinaryCloudName != null && !cloudinaryCloudName.isEmpty() &&
            cloudinaryApiKey != null && !cloudinaryApiKey.isEmpty()) ? "✓" : "✗");

    // Server
    String port = System.getenv("PORT");
    logger.info("🌐 SERVER:");
    logger.info("  Port: {}", port != null ? port : "8081 (default)");
    logger.info("  Profile: {}",
        System.getenv("SPRING_PROFILES_ACTIVE") != null ? System.getenv("SPRING_PROFILES_ACTIVE") : "default");
  }

  private void validateConfigurations() {
    Map<String, String> issues = new HashMap<>();

    // Check database
    if (dbUrl == null || dbUrl.isEmpty()) {
      issues.put("Database", "URL not configured");
    }

    // Check Firebase
    if (firebaseConfig == null || firebaseConfig.isEmpty()) {
      issues.put("Firebase", "Configuration missing");
    }

    // Check Cloudinary
    if (cloudinaryCloudName == null || cloudinaryCloudName.isEmpty() ||
        cloudinaryApiKey == null || cloudinaryApiKey.isEmpty()) {
      issues.put("Cloudinary", "Incomplete configuration");
    }

    if (!issues.isEmpty()) {
      logger.warn("⚠️  CONFIGURATION ISSUES FOUND:");
      issues.forEach((key, value) -> logger.warn("  {}: {}", key, value));
    } else {
      logger.info("✅ ALL CONFIGURATIONS VALIDATED SUCCESSFULLY");
    }
  }

  private String extractFirebaseProject(String json) {
    try {
      // Simple extraction of project_id from JSON
      if (json.contains("\"project_id\":")) {
        int start = json.indexOf("\"project_id\":\"") + 14;
        int end = json.indexOf("\"", start);
        if (start > 13 && end > start) {
          return json.substring(start, end);
        }
      }
    } catch (Exception e) {
      // Ignore extraction errors
    }
    return "Unknown";
  }

  private String maskSensitiveData(String value, String sensitiveKey) {
    if (value == null)
      return "null";
    if (value.contains(sensitiveKey + "=")) {
      return value.replaceAll(sensitiveKey + "=[^&]*", sensitiveKey + "=****");
    }
    return value;
  }

  private String maskValue(String value) {
    return maskValue(value, 0);
  }

  private String maskValue(String value, int visibleEndChars) {
    if (value == null || value.length() <= 4) {
      return value != null ? "****" : "null";
    }
    if (visibleEndChars > 0 && value.length() > visibleEndChars) {
      return "****" + value.substring(value.length() - visibleEndChars);
    }
    return "****" + value.substring(Math.max(0, value.length() - 4));
  }

  // Getters for other components to use
  public String getFirebaseConfig() {
    return firebaseConfig;
  }

  public boolean isFirebaseConfigured() {
    return firebaseConfig != null && !firebaseConfig.isEmpty();
  }
}
