package com.cinema.system.movie.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.common.response.PageResponse;
import com.cinema.system.movie.dto.MovieCreateRequest;
import com.cinema.system.movie.dto.MovieUpdateRequest;
import com.cinema.system.movie.entity.Movie;
import com.cinema.system.movie.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads", "images");

    public PageResponse<Movie> listMovies(String keyword, String genre, String status, int page, int size) {
        Page<Movie> moviePage = movieRepository.searchMovies(keyword, genre, status,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseDate")));
        return PageResponse.of(moviePage.getContent(), page, size, moviePage.getTotalElements());
    }

    public Movie getMovie(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new BusinessException("电影不存在"));
    }

    public Movie createMovie(MovieCreateRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setDescription(request.getDescription());
        movie.setDuration(request.getDuration());
        movie.setRating(request.getRating());
        movie.setGenre(request.getGenre());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setStatus(request.getStatus() != null ? request.getStatus() : "ON");
        return movieRepository.save(movie);
    }

    public Movie updateMovie(Long id, MovieUpdateRequest request) {
        Movie movie = getMovie(id);
        if (request.getPosterUrl() != null && !request.getPosterUrl().equals(movie.getPosterUrl())) {
            deleteImageFile(movie.getPosterUrl());
        }
        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDuration() != null) movie.setDuration(request.getDuration());
        if (request.getRating() != null) movie.setRating(request.getRating());
        if (request.getGenre() != null) movie.setGenre(request.getGenre());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getStatus() != null) movie.setStatus(request.getStatus());
        return movieRepository.save(movie);
    }

    public void hideMovie(Long id) {
        Movie movie = getMovie(id);
        movie.setStatus("OFF");
        movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        Movie movie = getMovie(id);
        deleteImageFile(movie.getPosterUrl());
        movieRepository.delete(movie);
    }

    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try {
            String relativePath = imageUrl.replace("/uploads/images/", "");
            Path filePath = uploadDir.resolve(relativePath);
            Files.deleteIfExists(filePath);
            // also try to delete parent directory if empty
            Path parentDir = filePath.getParent();
            if (parentDir != null && !parentDir.equals(uploadDir)) {
                try { Files.deleteIfExists(parentDir); } catch (IOException ignored) {}
            }
        } catch (IOException ignored) {
        }
    }
}
