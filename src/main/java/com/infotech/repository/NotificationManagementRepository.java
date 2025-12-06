package com.infotech.repository;

import com.infotech.entity.NotificationManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationManagementRepository extends JpaRepository<NotificationManagement, Long> {

}
