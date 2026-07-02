package com.cinema.system.order.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.common.response.PageResponse;
import com.cinema.system.order.dto.OrderCreateRequest;
import com.cinema.system.order.dto.OrderDetailResponse;
import com.cinema.system.order.entity.Order;
import com.cinema.system.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ApiResponse<Order> createOrder(@Valid @RequestBody OrderCreateRequest request,
                                          Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(orderService.createOrder(request, userId));
    }

    @GetMapping
    public ApiResponse<PageResponse<Order>> listOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(orderService.listOrders(userId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(orderService.getOrderDetail(id, userId));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Void> refundOrder(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.refundOrder(id, userId);
        return ApiResponse.success("退票成功", null);
    }
}
