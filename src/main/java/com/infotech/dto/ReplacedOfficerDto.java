package com.infotech.dto;

import lombok.Data;

@Data
public class ReplacedOfficerDto {

  private Long id;
  private Long assignmentId;

  private OfficerDto previousOfficer;
  private OfficerDto currentOfficer;

  private String reason;
  private String reasonMessage;
  private String acceptedBy;
  private String status;
}
