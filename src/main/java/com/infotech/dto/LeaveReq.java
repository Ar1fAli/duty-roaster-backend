package com.infotech.dto;

import lombok.Data;

@Data
public class LeaveReq {

  private Long id;
  private String status;
  private String reason;
  private boolean forceapply = false;

}
