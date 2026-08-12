package com.n2n.booking.dto;

import com.n2n.booking.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingSummaryDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingSummaryResponse {
        private boolean valid;
        private String message;
        private String promoCode;
        private String reservationToken;
        private DiscountType discountType;
        private BigDecimal discountValue;
        private BigDecimal subtotal;
        private BigDecimal discountAmount;
        private BigDecimal totalAmount;
        private List<SummaryItemResponse> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SummaryItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productCategory;
        private String imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private LocalDate bookingDate;
        private BigDecimal subtotal;
    }
}
