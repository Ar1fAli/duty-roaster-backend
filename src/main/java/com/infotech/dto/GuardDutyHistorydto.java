package com.infotech.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GuardDutyHistorydto {

  private Long assignmentId;
  private String vipname;
  private String designation;

  private LocalDateTime startTime;
  private LocalDateTime endTime;

  private String status;

}
