package com.infotech.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;

import lombok.Data;

@Entity
@Data
public class NotificationManagement {

    private Long notificationId;
    private Long notificationSenderId;

    private String notificationSender;
    private String notificationSenderName;
    private String notificationMessage;

    private boolean notificationStatus;

    private LocalDateTime notificationAssignTime;
    private LocalDateTime notificationReadTime;

}
