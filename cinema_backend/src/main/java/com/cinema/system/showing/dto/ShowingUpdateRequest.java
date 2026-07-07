package com.cinema.system.showing.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowingUpdateRequest {
    private Long movieId;
    private Long hallId;
    private LocalDate showDate;
    private LocalTime showTime;
    private BigDecimal basePrice;
}