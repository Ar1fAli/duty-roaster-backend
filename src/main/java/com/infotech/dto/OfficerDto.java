package com.infotech.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OfficerDto {

  private Long id;

  private String name;
  private String password;
  private String username;
  private String email;
  private String rank;
  private String status;
  private LocalDateTime createdTime;
  private String gender;
  private Long pnNumber;
  private Long adharNo;
  // private String reqstatus;
  // private String reasonmes;

  private Long experience;
  private Long contactno;
}
