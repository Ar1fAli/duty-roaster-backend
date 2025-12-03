package com.infotech.repository;

import java.util.List;

import com.infotech.entity.NotificationCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationCategoryRepository extends JpaRepository<NotificationCategory, Long> {

  List<NotificationCategory> findByCategory_Id(Long categoryId);
}
