package com.n2n.booking.repository;

import com.n2n.booking.entity.ProductSlotHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ProductSlotHoldRepository extends JpaRepository<ProductSlotHold, Long> {

    Optional<ProductSlotHold> findByUserIdAndProductIdAndBookingDate(Long userId, Long productId, LocalDate bookingDate);

    @Query("SELECT COALESCE(SUM(h.quantity), 0) FROM ProductSlotHold h WHERE h.product.id = :productId AND h.bookingDate = :bookingDate AND h.expiresAt > :now AND (:userId IS NULL OR h.user.id <> :userId)")
    int sumActiveHeldQuantityExcludingUser(
            @Param("productId") Long productId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUserId(Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);
}
