package com.infotech.controller;

import java.util.List;

import com.infotech.entity.NotificationCategory;
import com.infotech.entity.NotificationGuard;
import com.infotech.repository.NotificationCategoryRepository;
import com.infotech.repository.NotificationGuardRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/guard/{id}")
    public ResponseEntity<List<NotificationGuard>> getOfficerNotification(@PathVariable Long id) {
        return ResponseEntity.ok(notificationGuardRepository.findByOfficer_Id(id));
    }

    @GetMapping("/vip/{id}")
    public ResponseEntity<List<NotificationCategory>> getCategoryNotification(@PathVariable Long id) {
        System.out.println("vip id is this " + id);
        return ResponseEntity.ok(notificationCategoryRepository.findByCategory_Id(id));
    }

    @GetMapping("/guard/read/{id}")
    public ResponseEntity<?> OfficerMarkAsRead(@PathVariable Long id) {
        NotificationGuard noti = notificationGuardRepository.findById(id).orElseThrow();
        noti.setRead(true);
        notificationGuardRepository.save(noti);
        return ResponseEntity.ok("Notification Marked As Read");
    }

    @GetMapping("/vip/read/{id}")
    public ResponseEntity<?> CataegoryMarkAsRead(@PathVariable Long id) {
        NotificationCategory noti = notificationCategoryRepository.findById(id).orElseThrow();
        noti.setRead(true);
        notificationCategoryRepository.save(noti);
        return ResponseEntity.ok("Notification Marked As Read");
    }

}
