package com.cinema.system.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class LockSeatRequest {
    @NotNull(message = "Showing ID cannot be empty")
    private Long showingId;

    @NotEmpty(message = "Seat ID list cannot be empty")
    private List<Long> seatIds;
}
