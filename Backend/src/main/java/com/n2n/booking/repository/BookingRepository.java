package com.n2n.booking.repository;

import com.n2n.booking.entity.Booking;
import com.n2n.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Booking> findByBookingNo(String bookingNo);
    List<Booking> findAllByOrderByCreatedAtDesc();

    @Query("SELECT SUM(b.totalAmount - COALESCE(b.refundedAmount, 0)) FROM Booking b WHERE b.paymentStatus IN (com.n2n.booking.enums.PaymentStatus.PAID, com.n2n.booking.enums.PaymentStatus.PARTIALLY_REFUNDED)")
    BigDecimal calculateTotalRevenue();

    Long countByStatus(BookingStatus status);

    List<Booking> findByPaymentStatusOrderByCreatedAtDesc(com.n2n.booking.enums.PaymentStatus paymentStatus);

    Long countByPaymentStatus(com.n2n.booking.enums.PaymentStatus paymentStatus);
}
