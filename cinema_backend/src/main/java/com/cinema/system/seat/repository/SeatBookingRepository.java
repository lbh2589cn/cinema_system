package com.cinema.system.seat.repository;

import com.cinema.system.seat.entity.SeatBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatBookingRepository extends JpaRepository<SeatBooking, Long> {
    List<SeatBooking> findByShowingId(Long showingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatBooking s WHERE s.showingId = :showingId AND s.seatId IN :seatIds")
    List<SeatBooking> findByShowingIdAndSeatIdInWithLock(@Param("showingId") Long showingId,
                                                          @Param("seatIds") List<Long> seatIds);

    List<SeatBooking> findByShowingIdAndSeatIdIn(Long showingId, List<Long> seatIds);

    @Query("SELECT s FROM SeatBooking s WHERE s.status = 'LOCKED' AND s.lockedAt < :expireTime")
    List<SeatBooking> findExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    List<SeatBooking> findByOrderId(Long orderId);
}
