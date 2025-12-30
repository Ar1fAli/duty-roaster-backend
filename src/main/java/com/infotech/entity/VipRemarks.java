package com.infotech.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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
  @JoinTable(name = "vip_remarks_officers", joinColumns = @JoinColumn(name = "vip_remarks_id"), inverseJoinColumns = @JoinColumn(name = "officer_id"))
  private List<Officer> officersRemarks;

  @ManyToOne
  @JoinColumn(name = "assignmentId")
  private UserGuardAssignment assignment;

  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @Column(nullable = false)
  private String remarks;

  @Column(nullable = false)
  private String subject;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private LocalDateTime created;

  private LocalDateTime readtime;
}
