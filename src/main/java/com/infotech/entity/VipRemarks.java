package com.infotech.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Entity
@Data
public class VipRemarks {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "officerId", nullable = false)
  private Officer officer;
  @ManyToOne
  @JoinColumn(name = "vipId", nullable = false)
  private Category vip;

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
