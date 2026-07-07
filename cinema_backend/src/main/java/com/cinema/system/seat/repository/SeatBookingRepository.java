package com.cinema.system.seat.repository;

import com.cinema.system.seat.entity.SeatBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface SeatBookingRepository extends JpaRepository<SeatBooking, Long> {
    @Query("SELECT sb FROM SeatBooking sb WHERE sb.showingId = :showingId ORDER BY sb.seatId ASC")
    List<SeatBooking> findByShowingId(@Param("showingId") Long showingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatBooking s WHERE s.id IN :ids")
    List<SeatBooking> findByIdsWithLock(@Param("ids") List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatBooking s WHERE s.showingId = :showingId AND s.id IN :ids")
    List<SeatBooking> findByShowingIdAndIdInWithLock(@Param("showingId") Long showingId,
                                                      @Param("ids") List<Long> ids);

    @Query("SELECT s FROM SeatBooking s WHERE s.status = 'LOCKED' AND s.lockedAt < :expireTime")
    List<SeatBooking> findExpiredLocks(@Param("expireTime") LocalDateTime expireTime);

    List<SeatBooking> findByOrderId(Long orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SeatBooking s where s.seatId in :seatIds")
    void deleteBySeatIdIn(@Param("seatIds") Set<Long> seatIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SeatBooking s where s.showingId = :showingId")
    void deleteByShowingId(@Param("showingId") Long showingId);

    @Query("SELECT COUNT(s) FROM SeatBooking s WHERE s.showingId = :showingId AND s.status = 'BOOKED'")
    Long countBookedByShowingId(@Param("showingId") Long showingId);
}
