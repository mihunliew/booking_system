package com.n2n.booking.service;

import com.n2n.booking.dto.PaymentDTOs;
import com.n2n.booking.entity.Booking;
import com.n2n.booking.enums.BookingStatus;
import com.n2n.booking.enums.PaymentStatus;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final StripeService stripeService;

    @Transactional(readOnly = true)
    public List<PaymentDTOs.PaymentResponse> getAllPayments(PaymentStatus status) {
        List<Booking> bookings;
        if (status != null) {
            bookings = bookingRepository.findByPaymentStatusOrderByCreatedAtDesc(status);
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }
        return bookings.stream().map(this::mapToPaymentResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentDTOs.PaymentResponse getPaymentById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found for booking ID: " + id));
        return mapToPaymentResponse(booking);
    }

    @Transactional(readOnly = true)
    public PaymentDTOs.PaymentSummaryResponse getPaymentSummary() {
        BigDecimal totalRevenue = bookingRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long paidCount = bookingRepository.countByPaymentStatus(PaymentStatus.PAID);
        long pendingCount = bookingRepository.countByPaymentStatus(PaymentStatus.PENDING) 
                + bookingRepository.countByPaymentStatus(PaymentStatus.UNPAID);
        long failedCount = bookingRepository.countByPaymentStatus(PaymentStatus.FAILED);
        long refundedCount = bookingRepository.countByPaymentStatus(PaymentStatus.REFUNDED);
        long partiallyRefundedCount = bookingRepository.countByPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);

        return PaymentDTOs.PaymentSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .paidCount(paidCount)
                .pendingCount(pendingCount)
                .failedCount(failedCount)
                .refundedCount(refundedCount)
                .partiallyRefundedCount(partiallyRefundedCount)
                .build();
    }

    @Transactional
    public PaymentDTOs.PaymentResponse processRefund(Long id, PaymentDTOs.RefundRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        // 1. One-time refund constraint: check if already refunded or partially refunded
        if ((booking.getRefundedAmount() != null && booking.getRefundedAmount().compareTo(BigDecimal.ZERO) > 0) ||
            booking.getPaymentStatus() == PaymentStatus.REFUNDED ||
            booking.getPaymentStatus() == PaymentStatus.PARTIALLY_REFUNDED) {
            throw new com.n2n.booking.exception.BadRequestException("This booking payment has already been refunded once and cannot be refunded again.");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.n2n.booking.exception.BadRequestException("Refund amount must be greater than 0");
        }

        if (request.getAmount().compareTo(booking.getTotalAmount()) > 0) {
            throw new com.n2n.booking.exception.BadRequestException("Refund amount cannot exceed total booking amount of $" + booking.getTotalAmount());
        }

        // 2. Call Stripe Refund API if Stripe PaymentIntent ID exists
        if (booking.getStripePaymentIntentId() != null && !booking.getStripePaymentIntentId().trim().isEmpty()) {
            try {
                stripeService.processRefund(booking.getStripePaymentIntentId(), request.getAmount());
            } catch (Exception e) {
                throw new com.n2n.booking.exception.BadRequestException("Stripe refund execution failed: " + e.getMessage());
            }
        }

        // 3. Persist refund state in DB
        booking.setRefundedAmount(request.getAmount());

        if (request.getAmount().compareTo(booking.getTotalAmount()) >= 0) {
            booking.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            booking.setPaymentStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }

        // Automatically set booking status to CANCELLED upon refund
        booking.setStatus(BookingStatus.CANCELLED);

        Booking saved = bookingRepository.save(booking);
        return mapToPaymentResponse(saved);
    }

    private PaymentDTOs.PaymentResponse mapToPaymentResponse(Booking b) {
        return PaymentDTOs.PaymentResponse.builder()
                .id(b.getId())
                .bookingNo(b.getBookingNo())
                .userId(b.getUser().getId())
                .username(b.getUser().getUsername())
                .userFullName(b.getUser().getFullName())
                .userEmail(b.getUser().getEmail())
                .totalAmount(b.getTotalAmount())
                .amountPaid(b.getAmountPaid() != null ? b.getAmountPaid() : (b.getPaymentStatus() == PaymentStatus.PAID ? b.getTotalAmount() : BigDecimal.ZERO))
                .refundedAmount(b.getRefundedAmount() != null ? b.getRefundedAmount() : BigDecimal.ZERO)
                .paymentStatus(b.getPaymentStatus())
                .paymentMethod(b.getPaymentMethod() != null ? b.getPaymentMethod() : "Card")
                .stripePaymentIntentId(b.getStripePaymentIntentId())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
