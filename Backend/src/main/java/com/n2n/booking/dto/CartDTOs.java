package com.n2n.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CartDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddToCartRequest {
        @NotNull
        private Long productId;

        @NotNull
        @Min(1)
        private Integer quantity;

        @NotNull
        private LocalDate bookingDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateCartRequest {
        @NotNull
        @Min(1)
        private Integer quantity;

        @NotNull
        private LocalDate bookingDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemResponse {
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
