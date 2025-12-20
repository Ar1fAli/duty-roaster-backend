package com.infotech.dto;

import lombok.Data;

@Data
public class Accidentreq {

  private Long id;
  private String req;
  private String reason;
  private boolean forceapply;

}
