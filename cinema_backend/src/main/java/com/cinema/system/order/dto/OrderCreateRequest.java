package com.cinema.system.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "排片ID不能为空")
    private Long showingId;

    @NotEmpty(message = "座位预订ID列表不能为空")
    private List<Long> seatBookingIds;

    private List<SnackItem> snackItems;

    @Data
    public static class SnackItem {
        @NotNull(message = "零食ID不能为空")
        private Long snackId;

        @NotNull(message = "零食数量不能为空")
        private Integer quantity;
    }
}
