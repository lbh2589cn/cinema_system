package com.cinema.system.hall.dto;

import lombok.Data;

@Data
public class SeatTypeUpdate {
    private Integer rowNum;
    private Integer colNum;
    private String seatType;
}
