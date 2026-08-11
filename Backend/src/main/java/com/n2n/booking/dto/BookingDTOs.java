package com.n2n.booking.dto;

import com.n2n.booking.enums.BookingStatus;
import com.n2n.booking.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDTOs {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutRequest {
        private Long paymentSettingId;
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayBookingRequest {
        private String paymentMethod;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        private BookingStatus status;
        private PaymentStatus paymentStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingResponse {
        private Long id;
        private String bookingNo;
        private Long userId;
        private String username;
        private String userFullName;
        private BigDecimal totalAmount;
        private BookingStatus status;
        private PaymentStatus paymentStatus;
        private String paymentMethod;
        private String notes;
        private LocalDateTime createdAt;
        private String checkoutUrl;
        private String stripePaymentIntentId;
        private BigDecimal amountPaid;
        private List<BookingItemResponse> items;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private LocalDate bookingDate;
        private BigDecimal subtotal;
    }
}
