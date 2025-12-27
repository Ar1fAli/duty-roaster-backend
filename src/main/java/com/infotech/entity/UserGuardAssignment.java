package com.infotech.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Entity
@Table(name = "user_guard_assignments")
@Data
public class UserGuardAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vip_id", nullable = false)
  private Category vip;

  @ManyToMany
  @JoinTable(name = "assignment_guards", joinColumns = @JoinColumn(name = "assignment_id"), inverseJoinColumns = @JoinColumn(name = "officer_id"))
  private List<Officer> guards = new ArrayList<>();
  @OneToOne(fetch = FetchType.LAZY)
  private VipRemarks vipRemarks;

  private String status;
  private int timesAssigned;
  private LocalDateTime assignedAt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
  private LocalDateTime startAt;
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
  private LocalDateTime endAt;
  private LocalDateTime atEnd;

  @PrePersist
  public void prePersist() {
    if (assignedAt == null)
      assignedAt = LocalDateTime.now();
  }
}
