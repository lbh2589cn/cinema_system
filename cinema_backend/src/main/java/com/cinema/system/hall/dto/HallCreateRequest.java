package com.cinema.system.hall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HallCreateRequest {
    @NotBlank(message = "影厅名称不能为空")
    private String name;

    @NotNull(message = "行数不能为空")
    private Integer rows;

    @NotNull(message = "列数不能为空")
    private Integer cols;

    private String description;
}
