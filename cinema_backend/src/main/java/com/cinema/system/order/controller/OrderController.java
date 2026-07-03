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
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ApiResponse.success(orderService.getOrderDetail(id, userId, isAdmin));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Void> refundOrder(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.refundOrder(id, userId);
        return ApiResponse.success("退票成功", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrder(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        orderService.deleteOrder(id, userId);
        return ApiResponse.success("订单已删除", null);
    }
}
