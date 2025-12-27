package com.infotech.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Entity
@Data
public class AssignmentHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Category> category;

  @ManyToMany(fetch = FetchType.LAZY)
  private List<Officer> officer;

  private String status;
  private int timesAssigned;
  private LocalDateTime assignedAt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
  private LocalDateTime startAt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
  private LocalDateTime endAt;
  private LocalDateTime atEnd;

}
