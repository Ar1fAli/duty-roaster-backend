
package com.infotech.dto;

import lombok.Data;

@Data
public class AssignmentDetailDTO {
    private Long assignmentId;
    private Long guardId;
    private String guardName;
    private String guardRank;
    private Long vipId;
    private String vipName;
    private String status;
    private String startAt;
    private String endAt;
}
