package com.cinema.system.snack.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.snack.dto.SnackCreateRequest;
import com.cinema.system.snack.entity.Snack;
import com.cinema.system.snack.service.SnackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/snacks")
@RequiredArgsConstructor
public class SnackController {
    private final SnackService snackService;

    @GetMapping
    public ApiResponse<List<Snack>> listSnacks() {
        return ApiResponse.success(snackService.listSnacks());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Snack> createSnack(@Valid @RequestBody SnackCreateRequest request) {
        return ApiResponse.success(snackService.createSnack(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Snack> updateSnack(@PathVariable Long id, @RequestBody SnackCreateRequest request) {
        return ApiResponse.success(snackService.updateSnack(id, request));
    }
}
