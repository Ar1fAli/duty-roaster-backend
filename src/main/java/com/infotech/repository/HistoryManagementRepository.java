package com.infotech.repository;

import com.infotech.entity.HistoryManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryManagementRepository extends JpaRepository<HistoryManagement, Long> {

}
