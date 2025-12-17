// package com.infotech.config;

//
// import java.io.InputStream;
//
// import jakarta.annotation.PostConstruct;
//
// import com.google.auth.oauth2.GoogleCredentials;
// import com.google.firebase.FirebaseApp;
// import com.google.firebase.FirebaseOptions;
//
// import org.springframework.context.annotation.Configuration;
// import org.springframework.core.io.ClassPathResource;
//
// @Configuration
// public class FirebaseConfig {
//
//   @PostConstruct
//   public void init() throws Exception {
//     InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
//
//     FirebaseOptions options = FirebaseOptions.builder()
//         .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//         .build();
//
//     if (FirebaseApp.getApps().isEmpty()) {
//       FirebaseApp.initializeApp(options);
//     }
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
//
//
//
package com.infotech.config;

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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
public class FirebaseConfig {

  private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @PostConstruct
  public void init() {
    try {
      InputStream serviceAccount = getServiceAccountInputStream();

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .build();

      if (FirebaseApp.getApps().isEmpty()) {
        FirebaseApp.initializeApp(options);
        logger.info("✅ Firebase initialized successfully");
      }
    } catch (Exception e) {
      logger.error("❌ Failed to initialize Firebase", e);
      // Don't throw exception if it's optional for your app
      // throw new RuntimeException("Firebase initialization failed", e);
    }
  }

  private InputStream getServiceAccountInputStream() throws Exception {
    // Priority 1: Base64 encoded environment variable (cleanest for production)
    String firebaseConfigBase64 = System.getenv("FIREBASE_CONFIG_BASE64");

    if (firebaseConfigBase64 != null && !firebaseConfigBase64.trim().isEmpty()) {
      logger.info("Using Firebase config from FIREBASE_CONFIG_BASE64 environment variable");
      byte[] decodedBytes = Base64.getDecoder().decode(firebaseConfigBase64);
      return new ByteArrayInputStream(decodedBytes);
    }

    // Priority 2: Plain JSON environment variable
    String firebaseConfigJson = System.getenv("FIREBASE_CONFIG_JSON");
    if (firebaseConfigJson != null && !firebaseConfigJson.trim().isEmpty()) {
      logger.info("Using Firebase config from FIREBASE_CONFIG_JSON environment variable");
      return new ByteArrayInputStream(firebaseConfigJson.getBytes());
    }

    // Priority 3: Local file (for development)
    try {
      Resource resource = new ClassPathResource("firebase-service-account.json");
      if (resource.exists()) {
        logger.info("Using Firebase config from local file: firebase-service-account.json");
        return resource.getInputStream();
      }
    } catch (Exception e) {
      logger.warn("Local firebase-service-account.json not found");
    }

    // Priority 4: Try reading from properties file as last resort
    logger.warn("No Firebase configuration found. Firebase features will be disabled.");
    return null;
  }
}
