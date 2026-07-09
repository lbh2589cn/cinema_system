package com.cinema.system.showing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowingCreateRequest {
    @NotNull(message = "Movie ID cannot be empty")
    private Long movieId;

    @NotNull(message = "Hall ID cannot be empty")
    private Long hallId;

    @NotNull(message = "Show date cannot be empty")
    private LocalDate showDate;

    @NotNull(message = "Show time cannot be empty")
    private LocalTime showTime;

    @NotNull(message = "Base price cannot be empty")
    private BigDecimal basePrice;
}
