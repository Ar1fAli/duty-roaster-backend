package com.infotech.repository;

import com.infotech.entity.AdminEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepsitory extends JpaRepository<AdminEntity, Long> {
}
