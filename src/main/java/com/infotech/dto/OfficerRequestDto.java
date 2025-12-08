package com.infotech.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OfficerRequestDto {

    private String name;
    private String password; // raw password from client (optional on update)
    private String username;
    private String email;
    private String rank;
    private String status; // optional on create; default Active in service
    private Long experience;
    private Long contactno;
    private LocalDateTime createdTime;
}
