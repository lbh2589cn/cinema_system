package com.cinema.system.payment.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.payment.dto.PaymentRequest;
import com.cinema.system.payment.entity.Payment;
import com.cinema.system.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ApiResponse<Payment> pay(@Valid @RequestBody PaymentRequest request,
                                    Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(paymentService.processPayment(request, userId));
    }
}
