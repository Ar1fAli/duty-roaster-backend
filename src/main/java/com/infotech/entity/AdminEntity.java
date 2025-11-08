package com.infotech.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("admin_id")
    @Column(name = "admin_id")
    private Long adminId;

    @JsonProperty("admin_name")
    @Column(name = "admin_name")
    private String adminName;

    @JsonProperty("admin_username")
    @Column(name = "admin_username")
    private String adminUsername;

    @JsonProperty("admin_email")
    @Column(name = "admin_email")
    private String adminEmail;

    @JsonProperty("admin_password")
    @Column(name = "admin_password")
    private String adminPassword;

    @JsonProperty("contact_no")
    @Column(name = "contact_no")
    private Long contactNo;

    @JsonProperty("admin_status")
    @Column(name = "admin_status")
    private String adminStatus;

}
