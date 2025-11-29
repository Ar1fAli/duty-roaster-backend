package com.infotech.dto;

import java.util.List;

import com.infotech.entity.UserGuardAssignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponse {
    private List<AssignmentSummary> summary;
    private List<UserGuardAssignment> details;
}
