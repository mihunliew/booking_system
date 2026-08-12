package com.n2n.booking.repository;

import com.n2n.booking.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
