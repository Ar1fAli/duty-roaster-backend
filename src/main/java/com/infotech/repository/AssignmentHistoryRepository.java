package com.infotech.repository;

import com.infotech.entity.AssignmentHistoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentHistoryRepository extends JpaRepository<AssignmentHistoryEntity, Long> {

}
