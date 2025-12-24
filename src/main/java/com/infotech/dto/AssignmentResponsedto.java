package com.infotech.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.infotech.entity.Category;
import com.infotech.entity.Officer;
import com.infotech.entity.TaskEntity;

import lombok.Data;

@Data
public class AssignmentResponsedto {
  private Long id;

  private Category category;

  private List<Officer> officer;

  private TaskEntity task;

  private String status;
  private int timesAssigned;
  private LocalDateTime assignedAt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
  private LocalDateTime startAt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
  private LocalDateTime endAt;
  private LocalDateTime atEnd;

}
