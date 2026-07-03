package com.cinema.system.order.scheduler;

import com.cinema.system.order.entity.Order;
import com.cinema.system.order.repository.OrderRepository;
import com.cinema.system.payment.entity.Payment;
import com.cinema.system.payment.repository.PaymentRepository;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 每10秒执行一次，取消创建超过1分钟仍未支付的订单并释放座位
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void cancelExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(1);
        List<Order> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore("PENDING", expireTime);

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("发现 {} 个超时未支付订单，正在取消并释放座位", expiredOrders.size());

        for (Order order : expiredOrders) {
            try {
                order.setStatus("CANCELLED");
                orderRepository.save(order);

                // 同步更新支付记录为 CANCELLED
                List<Payment> payments = paymentRepository.findByOrderIdAndStatus(order.getId(), "PENDING");
                for (Payment payment : payments) {
                    payment.setStatus("CANCELLED");
                }
                paymentRepository.saveAll(payments);

                List<SeatBooking> bookings = seatBookingRepository.findByOrderId(order.getId());
                for (SeatBooking booking : bookings) {
                    booking.setStatus("AVAILABLE");
                    booking.setLockedBy(null);
                    booking.setLockedAt(null);
                    booking.setBookedBy(null);
                    booking.setBookedAt(null);
                    booking.setOrderId(null);
                }
                seatBookingRepository.saveAll(bookings);

                log.info("订单 {} 已取消，释放 {} 个座位", order.getOrderNo(), bookings.size());
            } catch (Exception e) {
                log.error("取消订单 {} 时发生异常", order.getOrderNo(), e);
            }
        }
    }
}
