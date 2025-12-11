package com.infotech.dto;

import lombok.Data;

@Data
public class LoginResponse {

  private String data;
  private String role;
  private String username;

}
