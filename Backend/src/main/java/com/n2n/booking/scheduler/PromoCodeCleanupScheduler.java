package com.n2n.booking.scheduler;

import com.n2n.booking.repository.PromoCodeReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromoCodeCleanupScheduler {

    private final PromoCodeReservationRepository reservationRepository;

    @Scheduled(cron = "0 */1 * * * *") // Run every 1 minute
    @Transactional
    public void cleanupExpiredReservations() {
        int deleted = reservationRepository.deleteExpiredReservations(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired promo code reservations", deleted);
        }
    }
}
