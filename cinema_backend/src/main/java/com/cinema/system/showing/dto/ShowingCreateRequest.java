package com.cinema.system.showing.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ShowingCreateRequest {
    @NotNull(message = "电影ID不能为空")
    private Long movieId;

    @NotNull(message = "影厅ID不能为空")
    private Long hallId;

    @NotNull(message = "放映日期不能为空")
    private LocalDate showDate;

    @NotNull(message = "放映时间不能为空")
    private LocalTime showTime;

    @NotNull(message = "基础票价不能为空")
    private BigDecimal basePrice;
}
