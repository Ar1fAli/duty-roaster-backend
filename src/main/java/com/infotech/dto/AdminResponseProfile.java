package com.infotech.dto;

import lombok.Data;

@Data
public class AdminResponseProfile {
    //
    private Long id;

    private String adminName;

    private String adminUsername;

    private String adminEmail;

    private Long contactNo;

    private String role;

    private String url;

}
