package com.infotech.dto;

import lombok.Data;

@Data
public class AdminRequestDto {

  private String adminName;

  private String adminUsername;

  private String adminEmail;

  private String adminPassword;

  private Long contactNo;

  private String role;

}
