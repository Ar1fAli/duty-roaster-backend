package com.infotech.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuardHistoryResponse {

  private List<AssignmentHistoryDto> assignments;
  private List<ReplacedOfficerDto> replacedOfficers;
}
