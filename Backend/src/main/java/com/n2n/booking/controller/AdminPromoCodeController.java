package com.n2n.booking.controller;

import com.n2n.booking.dto.PromoCodeDTOs;
import com.n2n.booking.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promocodes")
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PROMOCODES_READ')")
@RequiredArgsConstructor
public class AdminPromoCodeController {

    private final PromoCodeService promoCodeService;

    @GetMapping
    public ResponseEntity<List<PromoCodeDTOs.PromoCodeResponse>> getAllPromoCodes() {
        return ResponseEntity.ok(promoCodeService.getAllPromoCodes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromoCodeDTOs.PromoCodeResponse> getPromoCodeById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(promoCodeService.getPromoCodeById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PROMOCODES_CREATE')")
    public ResponseEntity<PromoCodeDTOs.PromoCodeResponse> createPromoCode(@Valid @RequestBody PromoCodeDTOs.PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.createPromoCode(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PROMOCODES_UPDATE')")
    public ResponseEntity<PromoCodeDTOs.PromoCodeResponse> updatePromoCode(@PathVariable(name = "id") Long id, @Valid @RequestBody PromoCodeDTOs.PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.updatePromoCode(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PROMOCODES_DELETE')")
    public ResponseEntity<Void> deletePromoCode(@PathVariable(name = "id") Long id) {
        promoCodeService.deletePromoCode(id);
        return ResponseEntity.noContent().build();
    }
}
