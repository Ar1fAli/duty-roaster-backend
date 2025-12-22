package com.infotech.dto;

import lombok.Data;

@Data
public class GuardProfileResponse {

  private Long id;

  private String name;
  private String username;
  private String email;
  private String status;
  private String url;
  private String rank;

  private String gender;
  private Long experience;

  private Long pnNumber;
  private Long adharNo;

  // private String reqstatus;
  // private String reasonmes;

  private Long contactno;

}
