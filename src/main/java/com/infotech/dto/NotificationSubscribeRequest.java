package com.infotech.dto;

import lombok.Data;

@Data
public class NotificationSubscribeRequest {

  private Long notificationSenderId;

  private String notificationSenderRole;
  private String notificationSenderName;
  private String notificationMessage;
  private String notificationToken;

}
