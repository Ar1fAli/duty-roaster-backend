package com.infotech.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infotech.entity.AdminEntity;

public interface AdminRepsitory extends JpaRepository<AdminEntity, Long> {

  Optional<AdminEntity> findByAdminUsername(String adminUsername);
}
