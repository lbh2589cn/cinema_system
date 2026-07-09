package com.cinema.system.showing.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallSeatRepository;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.dto.ShowingCreateRequest;
import com.cinema.system.showing.dto.ShowingUpdateRequest;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .orElseThrow(() -> new BusinessException("Showing not found"));
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

        // Generate seat booking records for this showing
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
        if (!"SCHEDULED".equals(showing.getStatus())) {
            throw new BusinessException("Only scheduled showings can be cancelled");
        }
        LocalDateTime showDateTime = showing.getShowDate().atTime(showing.getShowTime());
        if (LocalDateTime.now().isAfter(showDateTime) || LocalDateTime.now().isEqual(showDateTime)) {
            throw new BusinessException("Showing has already started, cannot cancel");
        }
        showing.setStatus("CANCELLED");
        showingRepository.save(showing);
    }

    @Transactional
    public Showing updateShowing(Long id, ShowingUpdateRequest request) {
        Showing showing = getShowing(id);
        if (request.getMovieId() != null) {
            showing.setMovieId(request.getMovieId());
        }
        if (request.getHallId() != null) {
            showing.setHallId(request.getHallId());
        }
        if (request.getShowDate() != null) {
            showing.setShowDate(request.getShowDate());
        }
        if (request.getShowTime() != null) {
            showing.setShowTime(request.getShowTime());
        }
        if (request.getBasePrice() != null) {
            showing.setBasePrice(request.getBasePrice());
        }
        return showingRepository.save(showing);
    }

    @Transactional
    public void restoreShowing(Long id) {
        Showing showing = getShowing(id);
        if (!"CANCELLED".equals(showing.getStatus())) {
            throw new BusinessException("Only cancelled showings can be restored");
        }
        LocalDateTime showDateTime = showing.getShowDate().atTime(showing.getShowTime());
        if (LocalDateTime.now().isAfter(showDateTime) || LocalDateTime.now().isEqual(showDateTime)) {
            throw new BusinessException("Showing has already started, cannot restore");
        }
        showing.setStatus("SCHEDULED");
        showingRepository.save(showing);
    }

    @Transactional
    public void deleteShowing(Long id) {
        Showing showing = getShowing(id);
        seatBookingRepository.deleteByShowingId(id);
        showingRepository.delete(showing);
    }
}
