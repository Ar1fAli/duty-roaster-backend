package com.infotech.dto;

import lombok.Data;

// @Data
// public class OfficerResponseDto {
//
//     private Long id;
//
//     private String name;
//     private String username;
//     private String email;
//     private String rank;
//     private String status;
//     private String url;
//     private Long experience;
//     private Long contactno;
// }
//
//
//

@Data
public class OfficerResponseDto {

  private Long id;
  private String name;
  private String username;
  private String email;
  private String rank;
  private String status;
  private Long experience;
  private Long contactno;

  // 🔹 NEW
  private String gender;
  private Long pnNumber;
  private Long adharNo;
}
