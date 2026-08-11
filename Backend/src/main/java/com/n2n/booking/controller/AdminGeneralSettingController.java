package com.n2n.booking.controller;

import com.n2n.booking.dto.SettingDTOs;
import com.n2n.booking.service.GeneralSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN')")
public class AdminGeneralSettingController {

    private final GeneralSettingService settingService;

    @GetMapping
    public ResponseEntity<List<SettingDTOs.SettingResponse>> getAllSettings() {
        return ResponseEntity.ok(settingService.getAllSettings());
    }

    @PostMapping
    public ResponseEntity<SettingDTOs.SettingResponse> createSetting(@RequestBody SettingDTOs.SettingRequest request) {
        return ResponseEntity.ok(settingService.createSetting(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SettingDTOs.SettingResponse> updateSetting(@PathVariable Long id, @RequestBody SettingDTOs.SettingRequest request) {
        return ResponseEntity.ok(settingService.updateSetting(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable Long id) {
        settingService.deleteSetting(id);
        return ResponseEntity.noContent().build();
    }
}
