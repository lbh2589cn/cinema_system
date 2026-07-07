package com.cinema.system.pricing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResult {
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
    private BigDecimal totalDiscount;
    private List<DiscountItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountItem {
        private String ruleName;
        private String description;
        private BigDecimal impact;
    }
}
