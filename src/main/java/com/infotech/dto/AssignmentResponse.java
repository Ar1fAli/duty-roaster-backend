package com.infotech.dto;

import java.util.List;

import com.infotech.entity.UserGuardAssignment;

import lombok.Data;

@Data
public class AssignmentResponse {
    private List<AssignmentSummary> summary;
    private List<UserGuardAssignment> details;
}
