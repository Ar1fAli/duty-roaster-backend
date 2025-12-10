package com.infotech.repository;

import java.util.Optional;

import com.infotech.entity.UserData;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataRepository extends JpaRepository<UserData, Long> {

  Optional<UserData> findByUsername(String username);

}
