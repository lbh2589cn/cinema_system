package com.cinema.system.hall.service;

import com.cinema.system.common.exception.BusinessException;
import com.cinema.system.hall.dto.HallCreateRequest;
import com.cinema.system.hall.entity.Hall;
import com.cinema.system.hall.entity.HallSeat;
import com.cinema.system.hall.repository.HallRepository;
import com.cinema.system.hall.repository.HallSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallRepository hallRepository;
    private final HallSeatRepository hallSeatRepository;

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
                seat.setSeatType("STANDARD");
                hallSeatRepository.save(seat);
            }
        }
        return hall;
    }
}
