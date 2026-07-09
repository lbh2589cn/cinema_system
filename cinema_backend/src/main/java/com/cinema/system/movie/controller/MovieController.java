package com.cinema.system.movie.controller;

import com.cinema.system.common.response.ApiResponse;
import com.cinema.system.common.response.PageResponse;
import com.cinema.system.movie.dto.MovieCreateRequest;
import com.cinema.system.movie.dto.MovieUpdateRequest;
import com.cinema.system.movie.entity.Movie;
import com.cinema.system.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping
    public ApiResponse<PageResponse<Movie>> listMovies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ApiResponse.success(movieService.listMovies(keyword, genre, status, page, size, sortBy, sortDir));
    }

    @GetMapping("/{id}")
    public ApiResponse<Movie> getMovie(@PathVariable Long id) {
        return ApiResponse.success(movieService.getMovie(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Movie> createMovie(@Valid @RequestBody MovieCreateRequest request) {
        return ApiResponse.success(movieService.createMovie(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Movie> updateMovie(@PathVariable Long id, @RequestBody MovieUpdateRequest request) {
        return ApiResponse.success(movieService.updateMovie(id, request));
    }

    @PutMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> hideMovie(@PathVariable Long id) {
        movieService.hideMovie(id);
        return ApiResponse.success("Deactivated", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ApiResponse.success("Deleted successfully", null);
    }
}
