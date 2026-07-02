package com.cinema.system.snack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SnackCreateRequest {
    @NotBlank(message = "零食名称不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String imageUrl;

    @NotNull(message = "库存不能为空")
    private Integer stock;
}
