package com.cinema.system.showing.repository;

import com.cinema.system.showing.entity.Showing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowingRepository extends JpaRepository<Showing, Long> {
    List<Showing> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);

    @Query("SELECT s FROM Showing s WHERE " +
           "(:movieId IS NULL OR s.movieId = :movieId) AND " +
           "(:date IS NULL OR s.showDate = :date)")
    List<Showing> findShowings(@Param("movieId") Long movieId,
                               @Param("date") LocalDate date);

    List<Showing> findByShowDateAndStatus(LocalDate showDate, String status);
}
