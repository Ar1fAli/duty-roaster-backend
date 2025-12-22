package com.infotech.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.infotech.dto.NotificationSubscribeRequest;
import com.infotech.entity.NotificationCategory;
import com.infotech.entity.NotificationManagement;
import com.infotech.repository.NotificationCategoryRepository;
import com.infotech.repository.NotificationGuardRepository;
import com.infotech.repository.NotificationManagementRepository;
import com.infotech.service.FcmService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationGuardRepository notificationGuardRepository;
  private final NotificationCategoryRepository notificationCategoryRepository;
  private final NotificationManagementRepository notificationManagementRepository;
  private final FcmService service;

  @GetMapping("/{id}/{role}")
  public ResponseEntity<List<NotificationManagement>> getOfficerNotification(@PathVariable Long id,
      @PathVariable String role) {
    return ResponseEntity
        .ok(notificationManagementRepository.findByNotificationSenderIdAndNotificationSender(id, role));
  }

  @GetMapping("/vip/{id}")
  public ResponseEntity<List<NotificationCategory>> getCategoryNotification(@PathVariable Long id) {
    // System.out.println("vip id is this " + id);
    return ResponseEntity.ok(notificationCategoryRepository.findByCategory_Id(id));
  }

  @GetMapping("/read/{role}/{id}")
  public ResponseEntity<?> OfficerMarkAsRead(@PathVariable String role, @PathVariable Long id) {
    NotificationManagement noti = notificationManagementRepository.findByNotificationId(id);
    noti.setNotificationStatus(true);
    noti.setNotificationReadTime(LocalDateTime.now());
    noti.setNotificationReadBy(role);
    notificationManagementRepository.save(noti);
    return ResponseEntity.ok("Notification Marked As Read");
  }

  @GetMapping("/vip/read/{id}")
  public ResponseEntity<?> CataegoryMarkAsRead(@PathVariable Long id) {
    NotificationCategory noti = notificationCategoryRepository.findById(id).orElseThrow();
    noti.setRead(true);
    notificationCategoryRepository.save(noti);
    return ResponseEntity.ok("Notification Marked As Read");
  }

  @GetMapping("/all")
  public ResponseEntity<?> NotificationSendAll() {
    List<NotificationManagement> noti = notificationManagementRepository.findAll();
    return ResponseEntity.ok(noti);
  }

  // @GetMapping("/read/{id}/{role}")
  // public ResponseEntity<?> NotificationManagementRead(@PathVariable Long id,
  // @PathVariable String role) {
  // NotificationManagement noti =
  // notificationManagementRepository.findById(id).orElseThrow();
  // noti.setNotificationStatus(true);
  // noti.setNotificationReadTime(LocalDateTime.now());
  // noti.setNotificationReadBy(role);
  // notificationManagementRepository.save(noti);
  //
  // return ResponseEntity.ok("Notification Marked As Read");
  // }

  @PostMapping("/subscribe")
  public String subscribe(@RequestBody NotificationSubscribeRequest req) throws Exception {

    NotificationManagement notification = new NotificationManagement();
    notification.setNotificationSenderId(req.getNotificationSenderId());
    notification.setNotificationSender(req.getNotificationSenderRole());
    notification.setNotificationSenderName(req.getNotificationSenderName());
    notification.setNotificationMessage(req.getNotificationMessage());
    notification.setNotificationToken(req.getNotificationToken());
    notification.setNotificationStatus(false);
    notification.setNotificationAssignTime(LocalDateTime.now());
    notificationManagementRepository.save(notification);

    service.send(req.getNotificationToken(), "subscribed notification", req.getNotificationMessage());
    return "Notification sent";
  }

  @PostMapping
  public String notify(@RequestBody Map<String, String> req) throws Exception {
    service.send(req.get("token"), req.get("title"), req.get("body"));
    return "Notification sent";
  }

  @GetMapping("/read/all/{role}/{senderId}")
  public ResponseEntity<?> OfficerMarkAllAsRead(
      @PathVariable String role,
      @PathVariable Long senderId) {

    List<NotificationManagement> notifications = notificationManagementRepository
        .findByNotificationSenderIdAndNotificationSender(senderId, role);

    if (notifications.isEmpty()) {
      return ResponseEntity.ok("No notifications found");
    }

    LocalDateTime now = LocalDateTime.now();

    for (NotificationManagement noti : notifications) {
      noti.setNotificationStatus(true);
      noti.setNotificationReadTime(now);
      noti.setNotificationReadBy(role);
    }

    notificationManagementRepository.saveAll(notifications);
    return ResponseEntity.ok("All Notifications Marked As Read");
  }

  @GetMapping("/read/all/{role}")
  public ResponseEntity<?> markAllAsReadByRole(@PathVariable String role) {

    List<NotificationManagement> notifications = notificationManagementRepository
        .findByNotificationSender(role);

    if (notifications.isEmpty()) {
      return ResponseEntity.ok("No unread notifications");
    }

    LocalDateTime now = LocalDateTime.now();

    for (NotificationManagement noti : notifications) {
      noti.setNotificationStatus(true);
      noti.setNotificationReadTime(now);
      noti.setNotificationReadBy(role); // admin or manager
    }

    notificationManagementRepository.saveAll(notifications);
    return ResponseEntity.ok("All notifications marked as read by " + role);
  }

}
