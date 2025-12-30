package com.infotech.repository;

import java.util.List;

import com.infotech.entity.VipRemarks;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VipRemarksRepository extends JpaRepository<VipRemarks, Long> {
  List<VipRemarks> findByCategory_Id(Long id);

}
