package com.cinema.system.showing.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.showing.dto.ShowingCreateRequest;
import com.cinema.system.showing.dto.ShowingUpdateRequest;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.service.ShowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showings")
@RequiredArgsConstructor
public class ShowingController {
    private final ShowingService showingService;

    @GetMapping
    public ApiResponse<List<Showing>> listShowings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) LocalDate date) {
        return ApiResponse.success(showingService.listShowings(movieId, date));
    }

    @GetMapping("/{id}")
    public ApiResponse<Showing> getShowing(@PathVariable Long id) {
        return ApiResponse.success(showingService.getShowing(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Showing> createShowing(@Valid @RequestBody ShowingCreateRequest request) {
        return ApiResponse.success(showingService.createShowing(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Showing> updateShowing(@PathVariable Long id, @RequestBody ShowingUpdateRequest request) {
        return ApiResponse.success(showingService.updateShowing(id, request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> cancelShowing(@PathVariable Long id) {
        showingService.cancelShowing(id);
        return ApiResponse.success("取消成功", null);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restoreShowing(@PathVariable Long id) {
        showingService.restoreShowing(id);
        return ApiResponse.success("恢复成功", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteShowing(@PathVariable Long id) {
        showingService.deleteShowing(id);
        return ApiResponse.success("删除成功", null);
    }
}
