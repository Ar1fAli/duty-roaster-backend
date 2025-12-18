package com.infotech.config;
//

// //
// // import java.io.InputStream;
// //
// // import jakarta.annotation.PostConstruct;
// //
// // import com.google.auth.oauth2.GoogleCredentials;
// // import com.google.firebase.FirebaseApp;
// // import com.google.firebase.FirebaseOptions;
// //
// // import org.springframework.context.annotation.Configuration;
// // import org.springframework.core.io.ClassPathResource;
// //
// // @Configuration
// // public class FirebaseConfig {
// //
// //   @PostConstruct
// //   public void init() throws Exception {
// //     InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
// //
// //     FirebaseOptions options = FirebaseOptions.builder()
// //         .setCredentials(GoogleCredentials.fromStream(serviceAccount))
// //         .build();
// //
// //     if (FirebaseApp.getApps().isEmpty()) {
// //       FirebaseApp.initializeApp(options);
// //     }
// //   }
// // }
// //
// //
// //
// //
// //
// //
// //
// //
// //
// //
// //
// //
// //
// package com.infotech.config;
//
// import java.io.ByteArrayInputStream;
// import java.io.InputStream;
// import java.util.Base64;
//
// import jakarta.annotation.PostConstruct;
//
// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.firebase.FirebaseApp;
// import com.google.firebase.FirebaseOptions;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.io.ClassPathResource;
// import org.springframework.core.io.Resource;
//
// @Configuration
// public class FirebaseConfig {
//
//   private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
//
//   @PostConstruct
//   public void init() {
//     try {
//       InputStream serviceAccount = getServiceAccountInputStream();
//
//       FirebaseOptions options = FirebaseOptions.builder()
//           .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//           .build();
//
//       if (FirebaseApp.getApps().isEmpty()) {
//         FirebaseApp.initializeApp(options);
//         logger.info("✅ Firebase initialized successfully");
//       }
//     } catch (Exception e) {
//       logger.error("❌ Failed to initialize Firebase", e);
//       // Don't throw exception if it's optional for your app
//       // throw new RuntimeException("Firebase initialization failed", e);
//     }
//   }
//
//   private InputStream getServiceAccountInputStream() throws Exception {
//     // Priority 1: Base64 encoded environment variable (cleanest for production)
//     String firebaseConfigBase64 = System.getenv("FIREBASE_CONFIG_BASE64");
//
//     if (firebaseConfigBase64 != null && !firebaseConfigBase64.trim().isEmpty()) {
//       logger.info("Using Firebase config from FIREBASE_CONFIG_BASE64 environment variable");
//       byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
//       return new ByteArrayInputStream(decodedBytes);
//     }
//
//     // Priority 2: Plain JSON environment variable
//     String firebaseConfigJson = System.getenv("FIREBASE_CONFIG_JSON");
//     if (firebaseConfigJson != null && !firebaseConfigJson.trim().isEmpty()) {
//       logger.info("Using Firebase config from FIREBASE_CONFIG_JSON environment variable");
//       return new ByteArrayInputStream(firebaseConfigJson.getBytes());
//     }
//
//     // Priority 3: Local file (for development)
//     try {
//       Resource resource = new ClassPathResource("firebase-service-account.json");
//       if (resource.exists()) {
//         logger.info("Using Firebase config from local file: firebase-service-account.json");
//         return resource.getInputStream();
//       }
//     } catch (Exception e) {
//       logger.warn("Local firebase-service-account.json not found");
//     }
//
//     // Priority 4: Try reading from properties file as last resort
//     logger.warn("No Firebase configuration found. Firebase features will be disabled.");
//     return null;
//   }
// }
//
//
//
//
//
//
//
//
//
//
//package com.infotech.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

import jakarta.annotation.PostConstruct;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @PostConstruct
  public void init() {
    try {
      String jsonContent = getFirebaseJsonContent();

      if (jsonContent == null || jsonContent.trim().isEmpty()) {
        logger.warn("No Firebase configuration found. Firebase features disabled.");
        return;
      }

      // Log a small snippet for debugging
      logger.debug("Firebase JSON content (first 200 chars): {}",
          jsonContent.length() > 200 ? jsonContent.substring(0, 200) + "..." : jsonContent);

      // Fix potential PEM encoding issues
      jsonContent = fixPemEncoding(jsonContent);

      try (InputStream serviceAccount = new ByteArrayInputStream(jsonContent.getBytes())) {
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

        if (FirebaseApp.getApps().isEmpty()) {
          FirebaseApp.initializeApp(options);
          logger.info("✅ Firebase initialized successfully");
        } else {
          logger.info("ℹ️  Firebase already initialized");
        }
      }
    } catch (Exception e) {
      logger.error("❌ Failed to initialize Firebase: {}", e.getMessage());
      logger.debug("Stack trace:", e);
    }
  }

  private String getFirebaseJsonContent() {
    try {
      // Try Base64 first
      String base64Config = System.getenv("FIREBASE_CONFIG_BASE64");
      if (base64Config != null && !base64Config.trim().isEmpty()) {
        logger.info("Using Firebase config from FIREBASE_CONFIG_BASE64");

        // Clean the Base64 string
        String cleanBase64 = base64Config.trim().replaceAll("\\s+", "");

        try {
          byte[] decodedBytes = Base64.getDecoder().decode(cleanBase64);
          String json = new String(decodedBytes);
          logger.info("✅ Base64 decoded successfully, {} bytes", decodedBytes.length);
          return json;
        } catch (IllegalArgumentException e) {
          logger.error("❌ Failed to decode Base64: {}", e.getMessage());
          // Try with URL-safe decoder
          try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(cleanBase64);
            String json = new String(decodedBytes);
            logger.info("✅ Base64 URL decoding successful");
            return json;
          } catch (Exception e2) {
            logger.error("❌ URL decoding also failed: {}", e2.getMessage());
          }
        }
      }

      // Try JSON string
      String jsonConfig = System.getenv("FIREBASE_CONFIG_JSON");
      if (jsonConfig != null && !jsonConfig.trim().isEmpty()) {
        logger.info("Using Firebase config from FIREBASE_CONFIG_JSON");
        return jsonConfig.trim();
      }

      // Try local file (development only)
      try {
        java.io.File file = new java.io.File("src/main/resources/firebase-service-account.json");
        if (file.exists()) {
          logger.info("Using Firebase config from local file");
          return new String(java.nio.file.Files.readAllBytes(file.toPath()));
        }
      } catch (Exception e) {
        // Ignore file not found
      }

      return null;

    } catch (Exception e) {
      logger.error("Error loading Firebase config: {}", e.getMessage());
      return null;
    }
  }

  private String fixPemEncoding(String json) {
    // This fixes PEM encoding issues in the private key
    // The issue is that + characters in Base64 might be getting corrupted

    // First, ensure proper newlines in the private key
    if (json.contains("-----BEGIN PRIVATE KEY-----")) {
      // Replace escaped newlines with actual newlines
      json = json.replace("\\n", "\n");

      // Ensure the private key section has proper line endings
      json = json.replace("-----BEGIN PRIVATE KEY-----\\n", "-----BEGIN PRIVATE KEY-----\n");
      json = json.replace("-----END PRIVATE KEY-----\\n", "-----END PRIVATE KEY-----\n");

      // Fix any other escaped characters
      json = json.replace("\\\"", "\"");
    }

    return json;
  }
}
//
//
//
//
