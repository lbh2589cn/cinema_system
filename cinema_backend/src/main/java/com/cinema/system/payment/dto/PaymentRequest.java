package com.cinema.system.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull(message = "Order ID cannot be empty")
    private Long orderId;

    @NotBlank(message = "Payment method cannot be empty")
    private String paymentMethod;
}
