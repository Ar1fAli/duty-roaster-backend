package com.infotech.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class VipRemarks {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToMany
  private List<Officer> officer;
  @ManyToOne
  @JoinColumn(name = "assignmentHistoryId")
  private AssignmentHistoryEntity assignmentHistory;

  @Column(name = "remarks", nullable = false)
  private String remarks;

  @Column(name = "created", nullable = false)
  private LocalDateTime created;

  @Column(name = "readtime")
  private LocalDateTime readtime;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "status", nullable = false)
  private String status;

}
