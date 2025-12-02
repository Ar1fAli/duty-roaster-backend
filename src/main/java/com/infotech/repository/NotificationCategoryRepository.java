package com.infotech.repository;

import com.infotech.entity.NotificationCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationCategoryRepository extends JpaRepository<NotificationCategory, Long> {
}
