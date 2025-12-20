package com.infotech.dto;

import lombok.Data;

@Data
public class SettingRequest {

  private String name;
  private String rank;
  private Integer value;
}
