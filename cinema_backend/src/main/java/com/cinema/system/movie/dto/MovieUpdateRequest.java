package com.cinema.system.movie.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MovieUpdateRequest {
    private String title;
    private String posterUrl;
    private String description;
    private Integer duration;
    private BigDecimal rating;
    private String genre;
    private LocalDate releaseDate;
    private String status;
}
