package com.cinema.system.movie.repository;

import com.cinema.system.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Page<Movie> findByStatus(String status, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE " +
           "(:keyword IS NULL OR m.title LIKE %:keyword%) AND " +
           "(:genre IS NULL OR m.genre LIKE %:genre%) AND " +
           "(:status IS NULL OR m.status = :status)")
    Page<Movie> searchMovies(@Param("keyword") String keyword,
                             @Param("genre") String genre,
                             @Param("status") String status,
                             Pageable pageable);

    List<Movie> findByStatusOrderByReleaseDateDesc(String status);
}
