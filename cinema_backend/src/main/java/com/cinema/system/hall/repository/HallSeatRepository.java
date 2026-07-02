package com.cinema.system.hall.repository;

import com.cinema.system.hall.entity.HallSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HallSeatRepository extends JpaRepository<HallSeat, Long> {
    List<HallSeat> findByHallId(Long hallId);
    void deleteByHallId(Long hallId);
}
