package com.cinema.system.order.dto;

import com.cinema.system.order.entity.OrderItem;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailResponse {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long showingId;
    private Integer seatCount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private List<OrderItem> items;
}
