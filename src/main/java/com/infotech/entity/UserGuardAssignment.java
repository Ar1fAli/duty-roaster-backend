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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Table(name = "user_guard_assignments")
@Getter
@Setter
@Entity
@ToString(exclude = { "category", "officers" })
@EqualsAndHashCode(exclude = { "category", "officers" })
public class UserGuardAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoryId", nullable = false)
  private Category category;

  @ManyToMany
  @JoinTable(name = "assignmentOfficer", joinColumns = @JoinColumn(name = "assignment_id"), inverseJoinColumns = @JoinColumn(name = "officer_id"))
  private List<Officer> officers = new ArrayList<>();
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
