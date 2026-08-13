package com.n2n.booking.dto;

import com.n2n.booking.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentResponse {
        private Long id;
        private String bookingNo;
        private Long userId;
        private String username;
        private String userFullName;
        private String userEmail;
        private BigDecimal totalAmount;
        private BigDecimal amountPaid;
        private BigDecimal refundedAmount;
        private PaymentStatus paymentStatus;
        private String paymentMethod;
        private String stripePaymentIntentId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentSummaryResponse {
        private BigDecimal totalRevenue;
        private long paidCount;
        private long pendingCount;
        private long failedCount;
        private long refundedCount;
        private long partiallyRefundedCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundRequest {
        @NotNull(message = "Refund amount is required")
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
        private BigDecimal amount;

        private String reason;
    }
}
