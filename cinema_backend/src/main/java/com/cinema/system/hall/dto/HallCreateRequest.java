package com.cinema.system.hall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HallCreateRequest {
    @NotBlank(message = "Hall name cannot be empty")
    private String name;

    @NotNull(message = "Row count cannot be empty")
    private Integer rows;

    @NotNull(message = "Column count cannot be empty")
    private Integer cols;

    private String description;

    private List<SeatTypeUpdate> seats;
}
