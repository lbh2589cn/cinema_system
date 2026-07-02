package com.cinema.system.seat.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallSeatRepository;
import com.cinema.system.seat.dto.LockSeatRequest;
import com.cinema.system.seat.dto.SeatStatusResponse;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatBookingRepository seatBookingRepository;
    private final HallSeatRepository hallSeatRepository;

    public List<SeatStatusResponse> getSeatsByShowing(Long showingId) {
        List<SeatBooking> bookings = seatBookingRepository.findByShowingId(showingId);
        if (bookings.isEmpty()) {
            throw new BusinessException("该场次暂无座位数据");
        }

        List<Long> seatIds = bookings.stream()
                .map(SeatBooking::getSeatId)
                .collect(Collectors.toList());
        List<HallSeat> hallSeats = hallSeatRepository.findAllById(seatIds);
        Map<Long, HallSeat> seatMap = hallSeats.stream()
                .collect(Collectors.toMap(HallSeat::getId, s -> s));

        List<SeatStatusResponse> result = new ArrayList<>();
        for (SeatBooking booking : bookings) {
            HallSeat hallSeat = seatMap.get(booking.getSeatId());
            if (hallSeat != null) {
                result.add(new SeatStatusResponse(
                        booking.getId(), booking.getSeatId(),
                        hallSeat.getRowNum(), hallSeat.getColNum(),
                        hallSeat.getSeatType(), booking.getStatus()));
            }
        }
        return result;
    }

    @Transactional
    public void lockSeats(LockSeatRequest request, Long userId) {
        List<SeatBooking> bookings = seatBookingRepository
                .findByShowingIdAndSeatIdInWithLock(request.getShowingId(), request.getSeatIds());

        for (SeatBooking booking : bookings) {
            if (!"AVAILABLE".equals(booking.getStatus())) {
                throw new BusinessException("座位 " + booking.getSeatId() + " 已被锁定或售出");
            }
            booking.setStatus("LOCKED");
            booking.setLockedBy(userId);
            booking.setLockedAt(LocalDateTime.now());
        }
        seatBookingRepository.saveAll(bookings);
    }

    @Transactional
    public void unlockSeats(Long showingId, List<Long> seatBookingIds, Long userId) {
        List<SeatBooking> bookings = seatBookingRepository
                .findByShowingIdAndSeatIdInWithLock(showingId, seatBookingIds);

        for (SeatBooking booking : bookings) {
            if (!"LOCKED".equals(booking.getStatus())) {
                continue;
            }
            if (!userId.equals(booking.getLockedBy())) {
                throw new BusinessException("无权释放该座位");
            }
            booking.setStatus("AVAILABLE");
            booking.setLockedBy(null);
            booking.setLockedAt(null);
        }
        seatBookingRepository.saveAll(bookings);
    }
}
