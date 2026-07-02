package com.cinema.system.admin.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.common.response.PageResponse;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;

    @GetMapping("/users")
    public ApiResponse<PageResponse<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size));
        return ApiResponse.success(PageResponse.of(userPage.getContent(), page, size, userPage.getTotalElements()));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (updates.containsKey("isMember")) {
            user.setIsMember((Boolean) updates.get("isMember"));
        }
        if (updates.containsKey("status")) {
            user.setStatus((String) updates.get("status"));
        }
        userRepository.save(user);
        return ApiResponse.success("更新成功", null);
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        return ApiResponse.success(stats);
    }
}
