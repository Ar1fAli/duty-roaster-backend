package com.infotech.repository;

import java.util.List;

import com.infotech.entity.LeaveRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

  List<LeaveRequest> findByOfficer_IdAndCurrent(Long officerId, boolean current);

  // If you want only one latest request:
  LeaveRequest findFirstByOfficer_IdAndCurrentOrderByIdDesc(Long officerId, boolean current);

}
