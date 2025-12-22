package com.infotech.repository;

import java.util.List;

import com.infotech.entity.NotificationManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationManagementRepository extends JpaRepository<NotificationManagement, Long> {

  List<NotificationManagement> findByNotificationSenderIdAndNotificationSender(Long notificationSenderId,
      String notificationSender);

  NotificationManagement findByNotificationId(Long notificationId);

  List<NotificationManagement> findByNotificationSender(String sender);

  NotificationManagement findTopByNotificationSenderIdAndNotificationSenderOrderByNotificationAssignTimeDesc(
      Long notificationSenderId,
      String notificationSender);

}
