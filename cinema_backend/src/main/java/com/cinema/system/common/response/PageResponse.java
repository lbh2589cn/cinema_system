package com.cinema.system.common.response;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long total;
    private int totalPages;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long total) {
        PageResponse<T> response = new PageResponse<>();
        response.content = content;
        response.page = page;
        response.size = size;
        response.total = total;
        response.totalPages = (int) Math.ceil((double) total / size);
        return response;
    }
}
