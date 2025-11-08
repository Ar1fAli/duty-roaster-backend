package com.infotech.repository;

import java.util.Optional;

import com.infotech.entity.AdminEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepsitory extends JpaRepository<AdminEntity, Long> {

    Optional<AdminEntity> findByAdminUsername(String username);
}
