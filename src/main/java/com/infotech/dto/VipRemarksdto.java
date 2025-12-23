
package com.infotech.dto;

import lombok.Data;

@Data
public class VipRemarksdto {

  private Long id;

  private Long vipId;
  private Long officerId;

  private String remarks;

  private String subject;

}
