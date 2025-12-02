package com.infotech.repository;

import java.util.List;

import com.infotech.entity.Accident;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccidentRepository extends JpaRepository<Accident, Long> {

  // List<Accident> findByOfficer_IdAndCurrent(Long officerId, boolean current);

  // AccidentRepository.java
  List<Accident> findByGuardData_IdAndReq(Long officerId, String req);

  List<Accident> findByGuardData_Id(Long officerId);

  // If you want only one latest request:
  // Accident findFirstByOfficer_IdAndCurrentOrderByIdDesc(Long officerId, boolean
  // current);

}
