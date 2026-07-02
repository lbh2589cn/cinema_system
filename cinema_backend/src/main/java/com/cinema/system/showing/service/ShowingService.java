package com.cinema.system.showing.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallSeatRepository;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.dto.ShowingCreateRequest;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowingService {
    private final ShowingRepository showingRepository;
    private final HallSeatRepository hallSeatRepository;
    private final SeatBookingRepository seatBookingRepository;

    public List<Showing> listShowings(Long movieId, LocalDate date) {
        return showingRepository.findShowings(movieId, date);
    }

    public Showing getShowing(Long id) {
        return showingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排片不存在"));
    }

    @Transactional
    public Showing createShowing(ShowingCreateRequest request) {
        Showing showing = new Showing();
        showing.setMovieId(request.getMovieId());
        showing.setHallId(request.getHallId());
        showing.setShowDate(request.getShowDate());
        showing.setShowTime(request.getShowTime());
        showing.setBasePrice(request.getBasePrice());
        showing.setStatus("SCHEDULED");
        showing = showingRepository.save(showing);

        // 为该场次生成座位预订记录
        List<HallSeat> hallSeats = hallSeatRepository.findByHallId(request.getHallId());
        for (HallSeat hallSeat : hallSeats) {
            SeatBooking booking = new SeatBooking();
            booking.setShowingId(showing.getId());
            booking.setSeatId(hallSeat.getId());
            booking.setStatus("AVAILABLE");
            booking.setVersion(0);
            seatBookingRepository.save(booking);
        }

        return showing;
    }

    @Transactional
    public void cancelShowing(Long id) {
        Showing showing = getShowing(id);
        showing.setStatus("CANCELLED");
        showingRepository.save(showing);
    }
}
