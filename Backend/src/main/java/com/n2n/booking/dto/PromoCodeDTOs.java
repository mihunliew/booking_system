package com.n2n.booking.dto;

import com.n2n.booking.enums.DiscountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromoCodeDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyPromoRequest {
        private String code;
        private String reservationToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromoCodeRequest {
        @NotBlank(message = "Promo code is required")
        private String code;

        @NotNull(message = "Discount type is required")
        private DiscountType discountType;

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than zero")
        private BigDecimal discountValue;

        private BigDecimal minSpend;
        private BigDecimal maxDiscount;
        private Integer usageLimit;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PromoCodeResponse {
        private Long id;
        private String code;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private BigDecimal minSpend;
        private BigDecimal maxDiscount;
        private Integer usageLimit;
        private Integer usedCount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Boolean isActive;
        private LocalDateTime createdAt;
    }
}
