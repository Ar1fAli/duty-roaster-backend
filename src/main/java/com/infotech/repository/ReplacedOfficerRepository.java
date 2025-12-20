
package com.infotech.repository;

import java.util.List;
import java.util.Optional;

import com.infotech.entity.Category;
import com.infotech.entity.ReplacedOfficerEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplacedOfficerRepository extends JpaRepository<ReplacedOfficerEntity, Long> {
  // ReplacedOfficerEntity findByCategoryAndStatus(Category category, String
  // status);

  List<ReplacedOfficerEntity> findByCategoryAndStatus(Category category, String status);

  Optional<ReplacedOfficerEntity> findFirstByCategoryAndStatusOrderByIdDesc(Category category, String status);

}
