package com.n2n.booking.controller;

import com.n2n.booking.dto.BookingDTOs;
import com.n2n.booking.security.UserPrincipal;
import com.n2n.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/checkout")
    public ResponseEntity<BookingDTOs.BookingResponse> checkout(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody BookingDTOs.CheckoutRequest request) {
        return ResponseEntity.ok(bookingService.checkout(currentUser.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<BookingDTOs.BookingResponse>> getUserBookings(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(bookingService.getUserBookings(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTOs.BookingResponse> getBookingById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(currentUser.getId(), id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<BookingDTOs.BookingResponse> payBooking(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestBody BookingDTOs.PayBookingRequest request) {
        return ResponseEntity.ok(bookingService.payBooking(currentUser.getId(), id, request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingDTOs.BookingResponse> cancelBooking(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(currentUser.getId(), id));
    }
}
