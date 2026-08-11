package com.n2n.booking.controller;

import com.n2n.booking.dto.BookingDTOs;
import com.n2n.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'BOOKINGS_READ')")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<BookingDTOs.BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(adminService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTOs.BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getBookingById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'BOOKINGS_UPDATE')")
    public ResponseEntity<BookingDTOs.BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody BookingDTOs.UpdateStatusRequest request) {
        return ResponseEntity.ok(adminService.updateBookingStatus(id, request));
    }
}
