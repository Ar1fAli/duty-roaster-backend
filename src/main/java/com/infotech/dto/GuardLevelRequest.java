package com.infotech.dto;

import lombok.Data;

@Data
public class GuardLevelRequest {
    private String guardLevel;
    private Integer numberOfGuards;
}
