package com.cinema.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Account cannot be empty")
    @Size(min = 3, max = 50, message = "Account must be 3-50 characters")
    private String userId;

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 1, max = 50, message = "Username must be 1-50 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 50, message = "Password must be 6-50 characters")
    private String password;

    private String phone;
}
