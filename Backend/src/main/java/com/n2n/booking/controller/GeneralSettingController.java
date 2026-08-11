package com.n2n.booking.controller;

import com.n2n.booking.dto.SettingDTOs;
import com.n2n.booking.service.GeneralSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class GeneralSettingController {

    private final GeneralSettingService settingService;

    @GetMapping("/payment-methods")
    public ResponseEntity<List<SettingDTOs.SettingResponse>> getActivePaymentMethods() {
        return ResponseEntity.ok(settingService.getActivePaymentMethods());
    }
}
