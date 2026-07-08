package com.cinema.system.hall.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.hall.dto.HallCreateRequest;
import com.cinema.system.hall.dto.HallUpdateRequest;
import com.cinema.system.hall.entity.Hall;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallRepository;
import com.cinema.system.hall.repository.HallSeatRepository;
import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import com.cinema.system.showing.entity.Showing;
import com.cinema.system.showing.repository.ShowingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;
    private final HallSeatRepository hallSeatRepository;
    private final ShowingRepository showingRepository;
    private final SeatBookingRepository seatBookingRepository;

    public List<Hall> listHalls() {
        return hallRepository.findAll();
    }

    public Map<String, Object> getHallDetail(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new BusinessException("影厅不存在"));
        List<HallSeat> seats = hallSeatRepository.findByHallId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("hall", hall);
        result.put("seats", seats);
        return result;
    }

    @Transactional
    public Hall createHall(HallCreateRequest request) {
        Hall hall = new Hall();
        hall.setName(request.getName());
        hall.setRows(request.getRows());
        hall.setCols(request.getCols());
        hall.setSeatCount(request.getRows() * request.getCols());
        hall.setDescription(request.getDescription());
        hall = hallRepository.save(hall);

        // 自动生成座位
        for (int r = 1; r <= request.getRows(); r++) {
            for (int c = 1; c <= request.getCols(); c++) {
                HallSeat seat = new HallSeat();
                seat.setHallId(hall.getId());
                seat.setRowNum(r);
                seat.setColNum(c);
                seat.setStatus("STANDARD");
                hallSeatRepository.save(seat);
            }
        }

        // 创建时自定义座位类型
        if (request.getSeats() != null && !request.getSeats().isEmpty()) {
            List<HallSeat> created = hallSeatRepository.findByHallId(hall.getId());
            for (HallSeat seat : created) {
                request.getSeats().stream()
                        .filter(s -> s.getRowNum().equals(seat.getRowNum()) && s.getColNum().equals(seat.getColNum()))
                        .findFirst()
                        .ifPresent(s -> {
                            if (!seat.getStatus().equals(s.getStatus())) {
                                seat.setStatus(s.getStatus());
                                hallSeatRepository.save(seat);
                            }
                        });
            }
        }

        return hall;
    }

    @Transactional
    public Hall updateHall(Long id, HallUpdateRequest request) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new BusinessException("影厅不存在"));

        boolean sizeChanged = !hall.getRows().equals(request.getRows()) || !hall.getCols().equals(request.getCols());

        hall.setName(request.getName());
        hall.setRows(request.getRows());
        hall.setCols(request.getCols());
        hall.setSeatCount(request.getRows() * request.getCols());
        hall.setDescription(request.getDescription());
        hall = hallRepository.save(hall);

        // 行列数有变化时，增量更新座位（已存在的座位保持 ID 不变）
        if (sizeChanged) {
            // 按 (row, col) 索引旧座位
            List<HallSeat> existingSeats = hallSeatRepository.findByHallId(id);
            java.util.Map<String, HallSeat> oldSeatMap = new java.util.HashMap<>();
            for (HallSeat s : existingSeats) {
                oldSeatMap.put(s.getRowNum() + "," + s.getColNum(), s);
            }

            List<HallSeat> addedSeats = new java.util.ArrayList<>();

            for (int r = 1; r <= request.getRows(); r++) {
                for (int c = 1; c <= request.getCols(); c++) {
                    String key = r + "," + c;
                    HallSeat old = oldSeatMap.remove(key);
                    if (old != null) {
                    } else {
                        HallSeat seat = new HallSeat();
                        seat.setHallId(hall.getId());
                        seat.setRowNum(r);
                        seat.setColNum(c);
                        seat.setStatus("STANDARD");
                        addedSeats.add(hallSeatRepository.save(seat));
                    }
                }
            }

            // 删除不再存在的旧座位
            Set<Long> removedIds = oldSeatMap.values().stream().map(HallSeat::getId).collect(java.util.stream.Collectors.toSet());
            if (!removedIds.isEmpty()) {
                hallSeatRepository.deleteBySeatIdIn(removedIds);
                // 同步清理这些座位在各场次中的预订记录
                seatBookingRepository.deleteBySeatIdIn(removedIds);
            }

            // 为新增座位创建各场次的预订记录
            if (!addedSeats.isEmpty()) {
                List<Showing> showings = showingRepository.findByHallId(id);
                Set<Long> addedIds = addedSeats.stream().map(HallSeat::getId).collect(Collectors.toSet());
                for (Showing showing : showings) {
                    for (Long seatId : addedIds) {
                        SeatBooking booking = new SeatBooking();
                        booking.setShowingId(showing.getId());
                        booking.setSeatId(seatId);
                        booking.setStatus("AVAILABLE");
                        booking.setVersion(0);
                        seatBookingRepository.save(booking);
                    }
                }
            }
        }

        // 更新座位类型
        if (request.getSeats() != null && !request.getSeats().isEmpty()) {
            List<HallSeat> existingSeats = hallSeatRepository.findByHallId(id);
            for (HallSeat seat : existingSeats) {
                request.getSeats().stream()
                        .filter(s -> s.getRowNum().equals(seat.getRowNum()) && s.getColNum().equals(seat.getColNum()))
                        .findFirst()
                        .ifPresent(s -> {
                            if (!seat.getStatus().equals(s.getStatus())) {
                                seat.setStatus(s.getStatus());
                                hallSeatRepository.save(seat);
                            }
                        });
            }
        }

        return hall;
    }

    @Transactional
    public void deleteHall(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new BusinessException("影厅不存在"));
        hallSeatRepository.deleteByHallId(id);
        hallRepository.delete(hall);
    }
}
