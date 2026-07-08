package com.cinema.system.seat.scheduler;

import com.cinema.system.seat.repository.SeatBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
        int released = seatBookingRepository.releaseExpiredLocks(expireTime);

        if (released > 0) {
            log.info("已释放 {} 个超时锁定的座位", released);
        }
    }
}
