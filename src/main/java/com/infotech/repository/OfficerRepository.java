package com.infotech.repository;

import com.infotech.model.Officer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficerRepository extends JpaRepository<Officer, Long> {
}
