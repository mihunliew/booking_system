package com.n2n.booking.controller;

import com.n2n.booking.dto.BookingSummaryDTOs;
import com.n2n.booking.dto.PromoCodeDTOs;
import com.n2n.booking.security.UserPrincipal;
import com.n2n.booking.service.BookingSummaryCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promocodes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final BookingSummaryCalculationService calculationService;

    @PostMapping("/apply")
    public ResponseEntity<BookingSummaryDTOs.BookingSummaryResponse> applyPromoCode(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody PromoCodeDTOs.ApplyPromoRequest request) {

        String code = request != null ? request.getCode() : null;
        BookingSummaryDTOs.BookingSummaryResponse response = calculationService.calculateAndApplyPromo(currentUser.getId(), code);

        if (!response.isValid()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
