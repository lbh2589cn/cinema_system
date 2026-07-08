package com.cinema.system.admin.controller;

import com.cinema.system.common.enums.PaymentStatusEnum;
import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.common.response.PageResponse;
import com.cinema.system.hall.entity.Hall;
import com.cinema.system.hall.repository.HallRepository;
import com.cinema.system.movie.repository.MovieRepository;
import com.cinema.system.order.entity.Order;
import com.cinema.system.order.repository.OrderItemRepository;
import com.cinema.system.order.repository.OrderRepository;
import com.cinema.system.payment.entity.Payment;
import com.cinema.system.payment.repository.PaymentRepository;
import com.cinema.system.pricing.entity.PricingRule;
import com.cinema.system.pricing.repository.PricingRuleRepository;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import com.cinema.system.snack.entity.Snack;
import com.cinema.system.snack.repository.SnackRepository;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final PaymentRepository paymentRepository;
    private final HallRepository hallRepository;
    private final SnackRepository snackRepository;
    private final ShowingRepository showingRepository;
    private final PricingRuleRepository pricingRuleRepository;

    @GetMapping("/users")
    public ApiResponse<PageResponse<User>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> userPage = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
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
        stats.put("totalMovies", movieRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalRevenue", paymentRepository.sumAmountByStatus(PaymentStatusEnum.SUCCESS.getCode()));
        return ApiResponse.success(stats);
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<Order>> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orderPage = orderRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return ApiResponse.success(PageResponse.of(orderPage.getContent(), page, size, orderPage.getTotalElements()));
    }

    @GetMapping("/payments")
    public ApiResponse<PageResponse<Map<String, Object>>> listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Payment> paymentPage = paymentRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        List<Map<String, Object>> list = paymentPage.getContent().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("paymentNo", p.getPaymentNo());
            m.put("amount", p.getAmount());
            m.put("paymentMethod", p.getPaymentMethod());
            m.put("status", p.getStatus());
            m.put("paidAt", p.getPaidAt());
            m.put("createdAt", p.getCreatedAt());
            m.put("orderId", p.getOrderId());
            // 查询关联订单信息
            orderRepository.findById(p.getOrderId()).ifPresent(o -> {
                m.put("orderNo", o.getOrderNo());
                m.put("userId", o.getUserId());
            });
            return m;
        }).collect(Collectors.toList());
        return ApiResponse.success(PageResponse.of(list, page, size, paymentPage.getTotalElements()));
    }

    @GetMapping("/halls")
    public ApiResponse<PageResponse<Hall>> listHalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Hall> hallPage = hallRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return ApiResponse.success(PageResponse.of(hallPage.getContent(), page, size, hallPage.getTotalElements()));
    }

    @GetMapping("/snacks")
    public ApiResponse<PageResponse<Snack>> listSnacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Snack> snackPage = snackRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return ApiResponse.success(PageResponse.of(snackPage.getContent(), page, size, snackPage.getTotalElements()));
    }

    @GetMapping("/showings")
    public ApiResponse<PageResponse<Showing>> listShowings(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Showing> showingPage = showingRepository.findShowingsPageable(null, date, PageRequest.of(page, size, Sort.by("id").ascending()));
        return ApiResponse.success(PageResponse.of(showingPage.getContent(), page, size, showingPage.getTotalElements()));
    }

    @GetMapping("/pricing-rules")
    public ApiResponse<PageResponse<PricingRule>> listPricingRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PricingRule> rulePage = pricingRuleRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        return ApiResponse.success(PageResponse.of(rulePage.getContent(), page, size, rulePage.getTotalElements()));
    }

    @Transactional
    @DeleteMapping("/orders/{id}")
    public ApiResponse<Void> hardDeleteOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        // 释放座位
        seatBookingRepository.findByOrderId(id).forEach(booking -> {
            booking.setStatus("AVAILABLE");
            booking.setLockedBy(null);
            booking.setLockedAt(null);
            booking.setBookedBy(null);
            booking.setBookedAt(null);
            booking.setOrderId(null);
        });

        // 删除订单明细
        orderItemRepository.deleteAll(orderItemRepository.findByOrderId(id));
        // 删除支付记录
        paymentRepository.deleteAll(paymentRepository.findByOrderId(id));
        // 删除订单
        orderRepository.delete(order);

        return ApiResponse.success("订单已永久删除", null);
    }

    @Transactional
    @DeleteMapping("/payments/{id}")
    public ApiResponse<Void> hardDeletePayment(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("支付记录不存在"));
        paymentRepository.delete(payment);
        return ApiResponse.success("支付记录已永久删除", null);
    }
}
