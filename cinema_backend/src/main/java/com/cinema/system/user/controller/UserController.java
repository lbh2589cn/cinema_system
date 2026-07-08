package com.cinema.system.user.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.user.dto.ChangePasswordRequest;
import com.cinema.system.user.dto.UserProfileResponse;
import com.cinema.system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public ApiResponse<Void> updateProfile(Authentication authentication, @RequestBody UserProfileResponse request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.updateProfile(userId, request);
        return ApiResponse.success("更新成功", null);
    }

    @PutMapping("/me/password")
    public ApiResponse<Void> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        userService.deleteAccount(userId);
        return ApiResponse.success("账号已注销", null);
    }
}
