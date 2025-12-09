package com.infotech.repository;

import com.infotech.entity.AssignmentValue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentValueRepository extends JpaRepository<AssignmentValue, Long> {

}
