package com.infotech.repository;

import com.infotech.entity.EditedOfficer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditedOfficerRepository extends JpaRepository<EditedOfficer, Long> {

}
