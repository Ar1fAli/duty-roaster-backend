
package com.infotech.repository;

import com.infotech.entity.ReplacedOfficerEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplacedOfficerRepository extends JpaRepository<ReplacedOfficerEntity, Long> {
  // ReplacedOfficerEntity findByCategoryAndStatus(Category category, String
  // status);

}
