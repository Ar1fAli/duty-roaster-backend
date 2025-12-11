package com.infotech.repository;

import java.util.Optional;

import com.infotech.entity.AdminEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepsitory extends JpaRepository<AdminEntity, Long> {

  // Optional<AdminEntity> findByAdminUsername(String adminUsername);

  Optional<AdminEntity> findByAdminEmail(String adminEmail);

  Optional<AdminEntity> findByUserData_Username(String username);

  Optional<AdminEntity> findByContactNo(Long contactNo);
}
