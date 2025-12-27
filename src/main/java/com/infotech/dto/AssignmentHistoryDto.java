package com.infotech.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class AssignmentHistoryDto {

  private Long id;
  private String status;
  private int timesAssigned;

  private LocalDateTime assignedAt;
  private LocalDateTime startAt;
  private LocalDateTime endAt;
  private LocalDateTime atEnd;

  private List<CategoryDto> categories;
  private List<OfficerDto> officers;
}
