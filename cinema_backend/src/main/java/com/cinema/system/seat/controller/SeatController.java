package com.cinema.system.seat.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.seat.dto.LockSeatRequest;
import com.cinema.system.seat.dto.SeatStatusResponse;
import com.cinema.system.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {
    private final SeatService seatService;

    @GetMapping("/showings/{showingId}/seats")
    public ApiResponse<List<SeatStatusResponse>> getSeats(@PathVariable Long showingId) {
        return ApiResponse.success(seatService.getSeatsByShowing(showingId));
    }

    @PostMapping("/seats/lock")
    public ApiResponse<Void> lockSeats(@Valid @RequestBody LockSeatRequest request,
                                       Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        seatService.lockSeats(request, userId);
        return ApiResponse.success("锁定成功", null);
    }

    @PostMapping("/seats/unlock")
    public ApiResponse<Void> unlockSeats(@Valid @RequestBody LockSeatRequest request,
                                         Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        seatService.unlockSeats(request.getShowingId(), request.getSeatIds(), userId);
        return ApiResponse.success("释放成功", null);
    }
}
