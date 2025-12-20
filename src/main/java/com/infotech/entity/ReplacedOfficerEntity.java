
package com.infotech.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class ReplacedOfficerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoryId", nullable = false)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "previousOfficerId", nullable = false)
  private Officer previousOfficer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "currentOfficerId", nullable = true)
  private Officer currentOfficer;

  private String reason;
  private String reasonMessage;
  private String acceptedBy;
  private String status;

}
