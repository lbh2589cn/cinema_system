package com.cinema.system.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "Showing ID cannot be empty")
    private Long showingId;

    @NotEmpty(message = "Seat booking IDs cannot be empty")
    private List<Long> seatBookingIds;

    private List<SnackItem> snackItems;

    @Data
    public static class SnackItem {
        @NotNull(message = "Snack ID cannot be empty")
        private Long snackId;

        @NotNull(message = "Snack quantity cannot be empty")
        private Integer quantity;
    }
}
