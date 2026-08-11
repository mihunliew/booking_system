package com.n2n.booking.service;

import com.n2n.booking.dto.SettingDTOs;
import com.n2n.booking.entity.GeneralSetting;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.GeneralSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralSettingService {

    private final GeneralSettingRepository settingRepository;

    public List<SettingDTOs.SettingResponse> getActivePaymentMethods() {
        return settingRepository.findBySettingTypeAndActiveTrue("PAYMENT_METHOD")
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SettingDTOs.SettingResponse> getAllSettings() {
        return settingRepository.findAll()
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public SettingDTOs.SettingResponse createSetting(SettingDTOs.SettingRequest request) {
        GeneralSetting setting = GeneralSetting.builder()
                .settingType(request.getSettingType())
                .name(request.getName())
                .providerKey(request.getProviderKey())
                .icon(request.getIcon())
                .description(request.getDescription())
                .active(request.isActive())
                .build();
        return mapToDTO(settingRepository.save(setting));
    }

    public SettingDTOs.SettingResponse updateSetting(Long id, SettingDTOs.SettingRequest request) {
        GeneralSetting setting = settingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found"));

        setting.setSettingType(request.getSettingType());
        setting.setName(request.getName());
        setting.setProviderKey(request.getProviderKey());
        setting.setIcon(request.getIcon());
        setting.setDescription(request.getDescription());
        setting.setActive(request.isActive());

        return mapToDTO(settingRepository.save(setting));
    }

    public void deleteSetting(Long id) {
        if (!settingRepository.existsById(id)) {
            throw new ResourceNotFoundException("Setting not found");
        }
        settingRepository.deleteById(id);
    }

    private SettingDTOs.SettingResponse mapToDTO(GeneralSetting setting) {
        return SettingDTOs.SettingResponse.builder()
                .id(setting.getId())
                .settingType(setting.getSettingType())
                .name(setting.getName())
                .providerKey(setting.getProviderKey())
                .icon(setting.getIcon())
                .description(setting.getDescription())
                .active(setting.isActive())
                .build();
    }
}
