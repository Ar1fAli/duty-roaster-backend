package com.infotech.repository;

import java.util.List;
import java.util.Optional;

import com.infotech.entity.Officer;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfficerRepository extends JpaRepository<Officer, Long> {

    List<Officer> findByRank(String rank);

    Optional<Officer> findByUsername(String username);

}
