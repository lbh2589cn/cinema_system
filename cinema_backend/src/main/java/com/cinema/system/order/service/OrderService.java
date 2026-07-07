package com.cinema.system.order.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.common.response.PageResponse;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallSeatRepository;
import com.cinema.system.order.dto.OrderCreateRequest;
import com.cinema.system.order.dto.OrderDetailResponse;
import com.cinema.system.order.entity.Order;
import com.cinema.system.order.entity.OrderItem;
import com.cinema.system.order.repository.OrderItemRepository;
import com.cinema.system.order.repository.OrderRepository;
import com.cinema.system.payment.entity.Payment;
import com.cinema.system.payment.repository.PaymentRepository;
import com.cinema.system.payment.service.PaymentService;
import com.cinema.system.pricing.service.PricingRuleService;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import com.cinema.system.snack.entity.Snack;
import com.cinema.system.snack.repository.SnackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SeatBookingRepository seatBookingRepository;
    private final HallSeatRepository hallSeatRepository;
    private final ShowingRepository showingRepository;
    private final SnackRepository snackRepository;
    private final PricingRuleService pricingRuleService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    private static final AtomicLong ORDER_SEQUENCE = new AtomicLong(0);

    @Transactional
    public Order createOrder(OrderCreateRequest request, Long userId) {
        // 1. 验证座位状态并计算座位总价
        List<SeatBooking> seatBookings = seatBookingRepository
                .findByIdsWithLock(request.getSeatBookingIds());

        for (SeatBooking booking : seatBookings) {
            if (!"LOCKED".equals(booking.getStatus())) {
                throw new BusinessException("座位 " + booking.getSeatId() + " 状态异常");
            }
            if (!userId.equals(booking.getLockedBy())) {
                throw new BusinessException("座位 " + booking.getSeatId() + " 不是您锁定的");
            }
        }
        Showing showing = showingRepository.findById(request.getShowingId())
                .orElseThrow(() -> new BusinessException("排片不存在"));
        LocalDateTime showDateTime = showing.getShowDate().atTime(showing.getShowTime());
        if (LocalDateTime.now().isAfter(showDateTime) || LocalDateTime.now().isEqual(showDateTime)) {
            throw new BusinessException("电影已开始放映，无法购票");
        }
        BigDecimal seatTotal = showing.getBasePrice().multiply(BigDecimal.valueOf(seatBookings.size()));

        // 2. 计算零食总价
        BigDecimal snackTotal = BigDecimal.ZERO;
        List<OrderItem> snackItems = new ArrayList<>();
        if (request.getSnackItems() != null) {
            for (OrderCreateRequest.SnackItem item : request.getSnackItems()) {
                Snack snack = snackRepository.findById(item.getSnackId())
                        .orElseThrow(() -> new BusinessException("零食不存在"));
                if (snack.getStock() < item.getQuantity()) {
                    throw new BusinessException("零食 " + snack.getName() + " 库存不足");
                }
                BigDecimal subtotal = snack.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                snackTotal = snackTotal.add(subtotal);

                OrderItem orderItem = new OrderItem();
                orderItem.setItemType("SNACK");
                orderItem.setItemId(snack.getId());
                orderItem.setItemName(snack.getName());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setUnitPrice(snack.getPrice());
                orderItem.setSubtotal(subtotal);
                snackItems.add(orderItem);

                snack.setStock(snack.getStock() - item.getQuantity());
                snackRepository.save(snack);
            }
        }

        // 3. 计算动态价格
        BigDecimal totalAmount = seatTotal.add(snackTotal);
        var discountResult = pricingRuleService.calculateDiscount(request.getShowingId(), userId, totalAmount, seatBookings.size());
        BigDecimal discountAmount = discountResult.getTotalDiscount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = discountResult.getFinalAmount();

        // 4. 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShowingId(request.getShowingId());
        order.setSeatCount(seatBookings.size());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFinalAmount(finalAmount);
        order.setStatus("PENDING");
        order = orderRepository.save(order);

        // 5. 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setAmount(finalAmount);
        payment.setStatus("PENDING");
        payment.setPaymentMethod(null);
        payment.setPaymentNo(paymentService.generatePaymentNo());
        paymentRepository.save(payment);

        // 6. 更新座位状态
        for (SeatBooking booking : seatBookings) {
            booking.setStatus("LOCKED");
            booking.setLockedBy(userId);
            booking.setLockedAt(LocalDateTime.now());
            booking.setOrderId(order.getId());
        }
        seatBookingRepository.saveAll(seatBookings);

        // 7. 创建订单明细
        BigDecimal seatPrice = showing.getBasePrice();
        List<Long> hallSeatIds = seatBookings.stream()
                .map(SeatBooking::getSeatId)
                .collect(Collectors.toList());
        Map<Long, HallSeat> hallSeatMap = hallSeatRepository.findAllById(hallSeatIds).stream()
                .collect(Collectors.toMap(HallSeat::getId, h -> h));
        for (SeatBooking seatItem : seatBookings) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setItemType("SEAT");
            item.setItemId(seatItem.getId());
            HallSeat hallSeat = hallSeatMap.get(seatItem.getSeatId());
            String seatName = hallSeat != null ? 
                    "座位 第" + hallSeat.getRowNum() + "排第" + hallSeat.getColNum() + "座" : 
                    "座位 #" + seatItem.getSeatId();
            item.setItemName(seatName);
            item.setQuantity(1);
            item.setUnitPrice(seatPrice);
            item.setSubtotal(seatPrice);
            orderItemRepository.save(item);
        }
        for (OrderItem snackItem : snackItems) {
            snackItem.setOrderId(order.getId());
            orderItemRepository.save(snackItem);
        }

        return order;
    }

    public PageResponse<Order> listOrders(Long userId, int page, int size) {
        Page<Order> orderPage = orderRepository.findVisibleByUserId(userId,
                PageRequest.of(page, size));
        return PageResponse.of(orderPage.getContent(), page, size, orderPage.getTotalElements());
    }

    public OrderDetailResponse getOrderDetail(Long id, Long userId, boolean isAdmin) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(id);

        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setOrderNo(order.getOrderNo());
        response.setUserId(order.getUserId());
        response.setShowingId(order.getShowingId());
        response.setSeatCount(order.getSeatCount());
        response.setTotalAmount(order.getTotalAmount());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());
        response.setStatus(order.getStatus());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());
        response.setItems(items);

        // 获取该订单的所有支付记录
        List<Payment> payments = paymentRepository.findByOrderId(id);
        List<OrderDetailResponse.PaymentRecord> paymentRecords = payments.stream().map(p -> {
            OrderDetailResponse.PaymentRecord r = new OrderDetailResponse.PaymentRecord();
            r.setId(p.getId());
            r.setPaymentNo(p.getPaymentNo());
            r.setAmount(p.getAmount());
            r.setPaymentMethod(p.getPaymentMethod());
            r.setStatus(p.getStatus());
            r.setPaidAt(p.getPaidAt());
            r.setCreatedAt(p.getCreatedAt());
            return r;
        }).collect(Collectors.toList());
        response.setPayments(paymentRecords);

        return response;
    }

    @Transactional
    public void refundOrder(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权退票");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new BusinessException("只有已支付的订单才能退票");
        }
        Showing showing = showingRepository.findById(order.getShowingId())
                .orElseThrow(() -> new BusinessException("排片不存在"));
        LocalDateTime showDateTime = showing.getShowDate().atTime(showing.getShowTime());
        if (LocalDateTime.now().isAfter(showDateTime) || LocalDateTime.now().isEqual(showDateTime)) {
            throw new BusinessException("电影已开始放映，无法退票");
        }

        order.setStatus("REFUNDED");
        orderRepository.save(order);

        // 释放座位
        List<SeatBooking> bookings = seatBookingRepository.findByOrderId(id);
        for (SeatBooking booking : bookings) {
            booking.setStatus("AVAILABLE");
            booking.setLockedBy(null);
            booking.setLockedAt(null);
            booking.setBookedBy(null);
            booking.setBookedAt(null);
            booking.setOrderId(null);
        }
        seatBookingRepository.saveAll(bookings);
    }

    @Transactional
    public void deleteOrder(Long id, Long userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException("订单不存在"));
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该订单");
        }

        order.setVisible(false);
        orderRepository.save(order);
    }

    private synchronized String generateOrderNo() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seq = ORDER_SEQUENCE.incrementAndGet() % 100000000;
        String orderNo = datePart + String.format("%08d", seq);
        while (orderRepository.findByOrderNo(orderNo).isPresent()) {
            seq = ORDER_SEQUENCE.incrementAndGet() % 100000000;
            orderNo = datePart + String.format("%08d", seq);
        }
        return orderNo;
    }
}
