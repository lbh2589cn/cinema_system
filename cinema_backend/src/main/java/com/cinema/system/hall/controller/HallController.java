package com.cinema.system.hall.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.hall.dto.HallCreateRequest;
import com.cinema.system.hall.dto.HallUpdateRequest;
import com.cinema.system.hall.entity.Hall;
import com.cinema.system.hall.service.HallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {
    private final HallService hallService;

    @GetMapping
    public ApiResponse<List<Hall>> listHalls() {
        return ApiResponse.success(hallService.listHalls());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getHallDetail(@PathVariable Long id) {
        return ApiResponse.success(hallService.getHallDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Hall> createHall(@Valid @RequestBody HallCreateRequest request) {
        return ApiResponse.success(hallService.createHall(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Hall> updateHall(@PathVariable Long id, @Valid @RequestBody HallUpdateRequest request) {
        return ApiResponse.success(hallService.updateHall(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteHall(@PathVariable Long id) {
        hallService.deleteHall(id);
        return ApiResponse.success(null);
    }
}
