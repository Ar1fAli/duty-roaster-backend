package com.infotech.dto;

import java.util.List;

import com.infotech.entity.ReplacedOfficerEntity;
import com.infotech.entity.UserGuardAssignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuardHistoryResponse {

  private List<UserGuardAssignment> assignments;
  private List<ReplacedOfficerEntity> replacedOfficers;
}
