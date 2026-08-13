package com.n2n.booking.controller;

import com.n2n.booking.dto.PaymentDTOs;
import com.n2n.booking.enums.PaymentStatus;
import com.n2n.booking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PAYMENTS_READ')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentDTOs.PaymentResponse>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getAllPayments(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTOs.PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<PaymentDTOs.PaymentSummaryResponse> getPaymentSummary() {
        return ResponseEntity.ok(paymentService.getPaymentSummary());
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PAYMENTS_UPDATE')")
    public ResponseEntity<PaymentDTOs.PaymentResponse> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody PaymentDTOs.RefundRequest request) {
        return ResponseEntity.ok(paymentService.processRefund(id, request));
    }
}
