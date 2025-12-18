package com.infotech.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebugController {

  private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

  @GetMapping("/api/debug/env")
  public String debugEnvironment() {
    String firebaseJson = System.getenv("FIREBASE_CONFIG_JSON");
    String firebaseBase64 = System.getenv("FIREBASE_CONFIG_BASE64");

    StringBuilder result = new StringBuilder();
    result.append("<h3>Firebase Environment Variables Debug</h3>");

    if (firebaseJson != null) {
      result.append("<h4>FIREBASE_CONFIG_JSON:</h4>");
      result.append("<pre>Length: ").append(firebaseJson.length()).append("</pre>");
      result.append("<pre>First 200 chars: ")
          .append(escapeHtml(firebaseJson.substring(0, Math.min(200, firebaseJson.length())))).append("</pre>");
      result.append("<pre>Last 200 chars: ")
          .append(escapeHtml(firebaseJson.substring(Math.max(0, firebaseJson.length() - 200)))).append("</pre>");

      // Check if it looks like Base64
      if (isBase64(firebaseJson)) {
        result.append("<p style='color: orange'>⚠️ This looks like Base64, not JSON!</p>");
      }
    } else {
      result.append("<p style='color: red'>FIREBASE_CONFIG_JSON is not set</p>");
    }

    if (firebaseBase64 != null) {
      result.append("<h4>FIREBASE_CONFIG_BASE64:</h4>");
      result.append("<pre>Length: ").append(firebaseBase64.length()).append("</pre>");
      result.append("<pre>First 100 chars: ")
          .append(firebaseBase64.substring(0, Math.min(100, firebaseBase64.length()))).append("</pre>");
    } else {
      result.append("<p>FIREBASE_CONFIG_BASE64 is not set</p>");
    }

    return result.toString();
  }

  private String escapeHtml(String text) {
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#039;");
  }

  private boolean isBase64(String str) {
    if (str == null || str.isEmpty())
      return false;
    // Base64 typically doesn't contain {, }, : characters
    return !str.contains("{") && !str.contains("}") && !str.contains(":");
  }
}
