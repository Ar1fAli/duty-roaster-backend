package com.infotech.dto;

import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;

    private String username;
    private String name;
    private String email;
    private String contactno;
    private String status;
    private String url;

}
