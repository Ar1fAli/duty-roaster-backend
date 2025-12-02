package com.infotech.controller;

import java.util.List;

import com.infotech.entity.NotificationGuard;
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

    private NotificationGuardRepository notificationGuardRepository;

    @GetMapping("/{id}")
    public ResponseEntity<List<NotificationGuard>> getCurrentLeave(@PathVariable Long id) {
        return ResponseEntity.ok(notificationGuardRepository.findByOfficer_Id(id));
    }

}
