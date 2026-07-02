package com.cinema.system.user.dto;

import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String userId;
    private String username;
    private String phone;
    private String nickname;
    private String role;
    private Boolean isMember;
    private String status;
}
