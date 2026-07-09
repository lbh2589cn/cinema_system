package com.cinema.system.order.scheduler;

import com.cinema.system.order.entity.Order;
import com.cinema.system.order.repository.OrderRepository;
import com.cinema.system.payment.repository.PaymentRepository;
import com.cinema.system.seat.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderRepository orderRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final PaymentRepository paymentRepository;

    private static final int BATCH_SIZE = 20;

    /**
     * Execute every 10 seconds, cancel orders unpaid for over 3 minutes and release seats
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void cancelExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrdersBatch("PENDING", BATCH_SIZE);

        if (expiredOrders.isEmpty()) {
            return;
        }

        List<Long> orderIds = expiredOrders.stream()
                .map(Order::getId)
                .toList();

        int cancelledCount = orderRepository.batchCancelOrders(orderIds);
        int paymentCount = paymentRepository.batchFailPayments(orderIds);
        int seatCount = seatBookingRepository.batchReleaseSeatsByOrderIds(orderIds);

        log.info("Batch cancelling {} timed-out unpaid orders, updating {} payment records to FAILED, releasing {} seats",
                cancelledCount, paymentCount, seatCount);
    }
}
