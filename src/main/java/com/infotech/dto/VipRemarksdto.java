
package com.infotech.dto;

import java.util.List;

import com.infotech.entity.AssignmentHistoryEntity;
import com.infotech.entity.Officer;
import com.infotech.entity.UserGuardAssignment;

import lombok.Data;

@Data
public class VipRemarksdto {

  private Long id;

  private AssignmentHistoryEntity assignmentHistory;

  private List<Officer> officers;

  private UserGuardAssignment currentAssignment;

  private Long officerId;

  private String remarks;

  private String subject;

}
