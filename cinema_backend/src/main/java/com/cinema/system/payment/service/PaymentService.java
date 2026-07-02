package com.cinema.system.payment.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.order.entity.Order;
import com.cinema.system.order.repository.OrderRepository;
import com.cinema.system.payment.dto.PaymentRequest;
import com.cinema.system.payment.entity.Payment;
import com.cinema.system.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    private static final AtomicLong PAYMENT_SEQUENCE = new AtomicLong(0);

    @Transactional
    public Payment processPayment(PaymentRequest request, Long userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权支付该订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("订单状态异常");
        }

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setPaymentNo(generatePaymentNo());
        payment.setAmount(order.getFinalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        order.setStatus("PAID");
        orderRepository.save(order);

        return payment;
    }

    private synchronized String generatePaymentNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 100000000; i++) {
            long seq = PAYMENT_SEQUENCE.incrementAndGet() % 100000000;
            String paymentNo = "PAY" + datePart + String.format("%08d", seq);
            if (paymentRepository.findByPaymentNo(paymentNo).isEmpty()) {
                return paymentNo;
            }
        }
        throw new BusinessException("支付编号生成失败，请稍后重试");
    }
}
