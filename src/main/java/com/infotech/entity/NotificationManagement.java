package com.infotech.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
@Data
public class NotificationManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    private Long notificationSenderId;

    private String notificationSender;
    private String notificationSenderName;
    private String notificationMessage;
    private String notificationReadBy;

    private boolean notificationStatus;

    private LocalDateTime notificationAssignTime;
    private LocalDateTime notificationReadTime;

}
