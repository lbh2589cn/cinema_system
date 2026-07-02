package com.cinema.system.seat.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class LockSeatRequest {
    @NotNull(message = "排片ID不能为空")
    private Long showingId;

    @NotEmpty(message = "座位ID列表不能为空")
    private List<Long> seatIds;
}
