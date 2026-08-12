package com.n2n.booking.repository;

import com.n2n.booking.entity.PromoCodeReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeReservationRepository extends JpaRepository<PromoCodeReservation, Long> {

    Optional<PromoCodeReservation> findByReservationToken(String reservationToken);

    Optional<PromoCodeReservation> findByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM PromoCodeReservation r WHERE r.promoCode.id = :promoCodeId AND r.expiresAt > :now")
    long countActiveReservationsByPromoCodeId(@Param("promoCodeId") Long promoCodeId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM PromoCodeReservation r WHERE r.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PromoCodeReservation r WHERE r.expiresAt < :now")
    int deleteExpiredReservations(@Param("now") LocalDateTime now);
}
