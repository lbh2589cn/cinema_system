package com.cinema.system.seat.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallSeatRepository;
import com.cinema.system.seat.dto.LockSeatRequest;
import com.cinema.system.seat.dto.SeatStatusResponse;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.user.entity.User;
import com.cinema.system.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatBookingRepository seatBookingRepository;
    private final HallSeatRepository hallSeatRepository;
    private final UserRepository userRepository;

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
                        hallSeat.getStatus(), booking.getStatus()));
            }
        }
        result.sort(Comparator.comparing(SeatStatusResponse::getRowNum)
                .thenComparing(SeatStatusResponse::getColNum));
        return result;
    }

    @Transactional
    public void lockSeats(LockSeatRequest request, Long userId) {
        List<SeatBooking> bookings = seatBookingRepository
                .findByShowingIdAndIdInWithLock(request.getShowingId(), request.getSeatIds());

        List<Long> seatIds = bookings.stream()
                .map(SeatBooking::getSeatId)
                .collect(Collectors.toList());
        List<HallSeat> hallSeats = hallSeatRepository.findAllById(seatIds);
        Map<Long, HallSeat> hallSeatMap = hallSeats.stream()
                .collect(Collectors.toMap(HallSeat::getId, s -> s));

        // 验证用户是否为会员（VIP 座位限制）
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        boolean isMember = user.getIsMember() != null && user.getIsMember();

        for (SeatBooking booking : bookings) {
            HallSeat hallSeat = hallSeatMap.get(booking.getSeatId());
            if (hallSeat != null && "UNAVAILABLE".equals(hallSeat.getStatus())) {
                throw new BusinessException("座位 " + booking.getSeatId() + " 已暂停使用");
            }
            if (hallSeat != null && "VIP".equals(hallSeat.getStatus()) && !isMember) {
                throw new BusinessException("VIP 座位仅限会员选购");
            }
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
                .findByShowingIdAndIdInWithLock(showingId, seatBookingIds);

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
