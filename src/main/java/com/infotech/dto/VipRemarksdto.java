
package com.infotech.dto;

import java.util.List;

import lombok.Data;

@Data
public class VipRemarksdto {

  private Long id;

  private Long currentAssignmentId;

  private List<Long> officerId;

  private String remarks;

  private String subject;

  private Long vipId;

}
