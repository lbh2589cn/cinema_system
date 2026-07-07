package com.cinema.system.payment.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.order.entity.Order;
import com.cinema.system.order.repository.OrderRepository;
import com.cinema.system.payment.dto.PaymentRequest;
import com.cinema.system.payment.entity.Payment;
import com.cinema.system.payment.repository.PaymentRepository;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final SeatBookingRepository seatBookingRepository;

    private static final AtomicLong PAYMENT_SEQUENCE = new AtomicLong(0);

    /**
     * 生成全局唯一的支付流水号
     */
    public String generatePaymentNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = PAYMENT_SEQUENCE.incrementAndGet() % 100000000;
        String paymentNo = "PAY" + datePart + String.format("%08d", seq);
        while (paymentRepository.findByPaymentNo(paymentNo).isPresent()) {
            seq = PAYMENT_SEQUENCE.incrementAndGet() % 100000000;
            paymentNo = "PAY" + datePart + String.format("%08d", seq);
        }
        return paymentNo;
    }

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

        // 查找该订单的待支付记录
        List<Payment> pendingPayments = paymentRepository.findByOrderIdAndStatus(request.getOrderId(), "PENDING");
        if (pendingPayments.isEmpty()) {
            throw new BusinessException("未找到待支付记录");
        }

        Payment payment = pendingPayments.get(0);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        order.setStatus("PAID");
        orderRepository.save(order);

        // 将座位从锁定更新为已售出
        List<SeatBooking> bookings = seatBookingRepository.findByOrderId(order.getId());
        for (SeatBooking booking : bookings) {
            booking.setStatus("BOOKED");
            booking.setBookedBy(userId);
            booking.setBookedAt(LocalDateTime.now());
        }
        seatBookingRepository.saveAll(bookings);

        return payment;
    }
}
