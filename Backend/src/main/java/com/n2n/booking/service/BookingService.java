package com.n2n.booking.service;

import com.n2n.booking.dto.BookingDTOs;
import com.n2n.booking.entity.Booking;
import com.n2n.booking.entity.BookingItem;
import com.n2n.booking.entity.CartItem;
import com.n2n.booking.entity.User;
import com.n2n.booking.enums.BookingStatus;
import com.n2n.booking.enums.PaymentStatus;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.BookingRepository;
import com.n2n.booking.repository.CartItemRepository;
import com.n2n.booking.repository.UserRepository;
import com.n2n.booking.entity.GeneralSetting;
import com.n2n.booking.repository.GeneralSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    private final GeneralSettingRepository settingRepository;

    @Transactional
    public BookingDTOs.BookingResponse checkout(Long userId, BookingDTOs.CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cannot checkout with an empty cart!");
        }

        GeneralSetting paymentSetting = settingRepository.findById(request.getPaymentSettingId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingItem> bookingItems = new ArrayList<>();

        String bookingNo = "BK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Booking booking = Booking.builder()
                .bookingNo(bookingNo)
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentMethod(paymentSetting.getName())
                .notes(request.getNotes())
                .build();

        for (CartItem cartItem : cartItems) {
            BigDecimal price = cartItem.getProduct().getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            BookingItem item = BookingItem.builder()
                    .booking(booking)
                    .product(cartItem.getProduct())
                    .productName(cartItem.getProduct().getName())
                    .price(price)
                    .quantity(cartItem.getQuantity())
                    .bookingDate(cartItem.getBookingDate())
                    .subtotal(subtotal)
                    .build();

            bookingItems.add(item);
        }

        booking.setTotalAmount(totalAmount);
        booking.setItems(bookingItems);

        Booking savedBooking = bookingRepository.save(booking);
        cartItemRepository.deleteByUserId(userId);

        // Call Stripe
        try {
            com.stripe.model.checkout.Session session = stripeService.createCheckoutSession(savedBooking, paymentSetting.getProviderKey());
            savedBooking.setStripeSessionId(session.getId());
            bookingRepository.save(savedBooking);
            
            BookingDTOs.BookingResponse response = mapToDTO(savedBooking);
            response.setCheckoutUrl(session.getUrl());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create Stripe session: " + e.getMessage(), e);
        }
    }

    public List<BookingDTOs.BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public BookingDTOs.BookingResponse getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to this booking");
        }

        return mapToDTO(booking);
    }

    @Transactional
    public BookingDTOs.BookingResponse payBooking(Long userId, Long bookingId, BookingDTOs.PayBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to this booking");
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setStatus(BookingStatus.CONFIRMED);
        if (request.getPaymentMethod() != null) {
            booking.setPaymentMethod(request.getPaymentMethod());
        }

        Booking updated = bookingRepository.save(booking);
        return mapToDTO(updated);
    }

    @Transactional
    public BookingDTOs.BookingResponse cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to this booking");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Completed bookings cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updated = bookingRepository.save(booking);
        return mapToDTO(updated);
    }

    public BookingDTOs.BookingResponse mapToDTO(Booking booking) {
        List<BookingDTOs.BookingItemResponse> itemDTOs = booking.getItems().stream()
                .map(item -> BookingDTOs.BookingItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .quantity(item.getQuantity())
                        .bookingDate(item.getBookingDate())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return BookingDTOs.BookingResponse.builder()
                .id(booking.getId())
                .bookingNo(booking.getBookingNo())
                .userId(booking.getUser().getId())
                .username(booking.getUser().getUsername())
                .userFullName(booking.getUser().getFullName())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .items(itemDTOs)
                .build();
    }
}
