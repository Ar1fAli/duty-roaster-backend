package com.infotech.dto;

import lombok.Data;

@Data
public class VipProfileResponse {
    private Long id;

    private Long contactno;

    private String name;
    private String email;
    private String username;
    private String password;
    private String status;

    private String url;

}
