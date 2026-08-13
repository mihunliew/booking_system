package com.n2n.booking.repository;

import com.n2n.booking.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    List<BookingItem> findByBookingId(Long bookingId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(bi.quantity), 0) FROM BookingItem bi WHERE bi.product.id = :productId AND bi.bookingDate = :bookingDate AND bi.booking.status <> com.n2n.booking.enums.BookingStatus.CANCELLED")
    int sumConfirmedBookedQuantity(@org.springframework.data.repository.query.Param("productId") Long productId, @org.springframework.data.repository.query.Param("bookingDate") java.time.LocalDate bookingDate);
}
