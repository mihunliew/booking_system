package com.n2n.booking.repository;

import com.n2n.booking.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndProductIdAndBookingDate(Long userId, Long productId, java.time.LocalDate bookingDate);
    void deleteByUserId(Long userId);
}
