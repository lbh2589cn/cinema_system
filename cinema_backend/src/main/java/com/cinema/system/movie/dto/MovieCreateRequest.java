package com.cinema.system.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovieCreateRequest {
    @NotBlank(message = "电影名称不能为空")
    private String title;

    private String posterUrl;
    private String description;

    @NotNull(message = "时长不能为空")
    private Integer duration;

    private BigDecimal rating;
    private String genre;
    private LocalDate releaseDate;
    private String status;
}
