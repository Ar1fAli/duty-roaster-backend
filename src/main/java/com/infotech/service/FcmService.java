package com.infotech.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

  public void send(String token, String title, String body) {
    if (token == null || token.trim().isEmpty()) {
      throw new IllegalArgumentException("FCM token cannot be null or empty");
    }
    log.info("token is this ----- :  " + token);

    try {
      Message message = Message.builder()
          .setToken(token) // Make sure token is set
          .setNotification(Notification.builder()
              .setTitle(title)
              .setBody(body)
              .build())
          .build();

      String response = FirebaseMessaging.getInstance().send(message);
      log.info("Successfully sent message: {}", response);
    } catch (Exception e) {
      log.error("Error sending FCM notification", e);
      throw new RuntimeException("Failed to send notification", e);
    }
  }

  public void sendNotificationSafely(String token, String title, String body, String entityType, Long entityId) {
    if (token == null || token.trim().isEmpty()) {
      log.warn("Skipping FCM notification for {} {}: Token is null or empty", entityType, entityId);
      return;
    }

    try {
      log.info("Successfully sending FCM notification to {} {}: {}   token is =={}==", entityType, entityId, title,
          token);
      send(token, title, body);
      log.info("Successfully sent FCM notification to {} {}: {}   token is =={}==", entityType, entityId, title, token);
    } catch (IllegalArgumentException e) {
      // Specifically catch the token validation error
      log.error("Invalid FCM token for {} {}: {}", entityType, entityId, e.getMessage());
    } catch (Exception e) {
      log.error("FCM send failed for {} {}: {}", entityType, entityId, e.getMessage(), e);
    }
  }

}
