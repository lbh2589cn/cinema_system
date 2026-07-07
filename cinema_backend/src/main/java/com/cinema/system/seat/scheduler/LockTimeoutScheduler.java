package com.cinema.system.seat.scheduler;

import com.cinema.system.seat.entity.SeatBooking;
import com.cinema.system.seat.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockTimeoutScheduler {

    private final SeatBookingRepository seatBookingRepository;

    /**
     * 每10秒执行一次，释放锁定超过3分钟的座位
     */
    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void releaseExpiredLocks() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(3);
        List<SeatBooking> expiredLocks = seatBookingRepository.findExpiredLocks(expireTime);

        if (expiredLocks.isEmpty()) {
            return;
        }

        log.info("发现 {} 个超时锁定的座位，正在释放", expiredLocks.size());

        for (SeatBooking booking : expiredLocks) {
            try {
                booking.setStatus("AVAILABLE");
                booking.setLockedBy(null);
                booking.setLockedAt(null);
            } catch (Exception e) {
                log.error("释放座位 {} 时发生异常", booking.getId(), e);
            }
        }
        seatBookingRepository.saveAll(expiredLocks);
        log.info("已释放 {} 个超时锁定的座位", expiredLocks.size());
    }
}
