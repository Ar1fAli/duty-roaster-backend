package com.infotech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssignmentSummary {
    private String level;
    private Integer requested;
    private Integer assigned;
    private Integer missing;
}
