package com.infotech.dto;

import lombok.Data;

// @Data
// public class OfficerRequestDto {
//
//     private String name;
//     private String password; // raw password from client (optional on update)
//     private String username;
//     private String email;
//     private String rank;
//     private String status; // optional on create; default Active in service
//     private Long experience;
//     private Long contactno;
//     private LocalDateTime createdTime;
// }
//
//
//

@Data
public class OfficerRequestDto {

  private String name;
  private String username;
  private String password;
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
