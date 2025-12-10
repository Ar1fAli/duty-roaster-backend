package com.infotech.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin_entity") // or your actual table name
@Getter
@Setter
public class AdminEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "admin_name")
  private String adminName;

  @Column(name = "admin_username", unique = true)
  private String adminUsername;

  @Column(name = "admin_email")
  private String adminEmail;

  @Column(name = "admin_password")
  private String adminPassword;

  @Column(name = "contact_no")
  private Long contactNo;

  @Column(name = "role")
  private String role;

  @JoinColumn(nullable = true)
  @OneToOne(fetch = FetchType.LAZY)
  @JsonIgnore
  private ProfilePicture pic;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = true)
  private UserData userData;
}
