package com.cinema.system.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Account cannot be empty")
    private String userId;
    
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
