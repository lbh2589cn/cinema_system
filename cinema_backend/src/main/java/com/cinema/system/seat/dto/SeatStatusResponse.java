package com.cinema.system.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SeatStatusResponse {
    private Long id;
    private Long seatId;
    private Integer rowNum;
    private Integer colNum;
    private String seatType;
    private String status;
}
