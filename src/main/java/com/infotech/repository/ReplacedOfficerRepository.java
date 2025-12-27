
package com.infotech.repository;

import java.util.List;

import com.infotech.entity.ReplacedOfficerEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplacedOfficerRepository extends JpaRepository<ReplacedOfficerEntity, Long> {

  List<ReplacedOfficerEntity> findByUserGuardAssignmentId(Long id);
  // ReplacedOfficerEntity findByCategoryAndStatus(Category category, String
  // status);

}
