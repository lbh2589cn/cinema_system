package com.cinema.system.snack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SnackCreateRequest {
    @NotBlank(message = "Snack name cannot be empty")
    private String name;

    @NotNull(message = "Price cannot be empty")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "Stock cannot be empty")
    private Integer stock;

    private String status;
}
