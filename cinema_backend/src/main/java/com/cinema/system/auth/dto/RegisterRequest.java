package com.cinema.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "账号不能为空")
    @Size(min = 3, max = 50, message = "账号长度3-50个字符")
    private String userId;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 50, message = "用户名长度1-50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度6-50个字符")
    private String password;

    private String phone;
}
