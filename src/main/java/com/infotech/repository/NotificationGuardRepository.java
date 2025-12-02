package com.infotech.repository;

import java.util.List;

import com.infotech.entity.NotificationGuard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationGuardRepository extends JpaRepository<NotificationGuard, Long> {
  List<NotificationGuard> findByOfficer_Id(Long officerId);

}
