package com.n2n.booking.service;

import com.n2n.booking.dto.PromoCodeDTOs;
import com.n2n.booking.entity.PromoCode;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public List<PromoCodeDTOs.PromoCodeResponse> getAllPromoCodes() {
        return promoCodeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PromoCodeDTOs.PromoCodeResponse getPromoCodeById(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found with id: " + id));
        return mapToDTO(promo);
    }

    @Transactional
    public PromoCodeDTOs.PromoCodeResponse createPromoCode(PromoCodeDTOs.PromoCodeRequest request) {
        String cleanCode = request.getCode().trim().toUpperCase();
        if (promoCodeRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new BadRequestException("Promo code already exists: " + cleanCode);
        }

        PromoCode promo = PromoCode.builder()
                .code(cleanCode)
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minSpend(request.getMinSpend() != null ? request.getMinSpend() : java.math.BigDecimal.ZERO)
                .maxDiscount(request.getMaxDiscount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        PromoCode saved = promoCodeRepository.save(promo);
        return mapToDTO(saved);
    }

    @Transactional
    public PromoCodeDTOs.PromoCodeResponse updatePromoCode(Long id, PromoCodeDTOs.PromoCodeRequest request) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promo code not found with id: " + id));

        String cleanCode = request.getCode().trim().toUpperCase();
        if (!promo.getCode().equalsIgnoreCase(cleanCode) && promoCodeRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new BadRequestException("Promo code already exists: " + cleanCode);
        }

        promo.setCode(cleanCode);
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        if (request.getMinSpend() != null) promo.setMinSpend(request.getMinSpend());
        promo.setMaxDiscount(request.getMaxDiscount());
        promo.setUsageLimit(request.getUsageLimit());
        if (request.getStartDate() != null) promo.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) promo.setEndDate(request.getEndDate());
        if (request.getIsActive() != null) promo.setIsActive(request.getIsActive());

        PromoCode updated = promoCodeRepository.save(promo);
        return mapToDTO(updated);
    }

    @Transactional
    public void deletePromoCode(Long id) {
        if (!promoCodeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Promo code not found with id: " + id);
        }
        promoCodeRepository.deleteById(id);
    }

    public PromoCodeDTOs.PromoCodeResponse mapToDTO(PromoCode promo) {
        return PromoCodeDTOs.PromoCodeResponse.builder()
                .id(promo.getId())
                .code(promo.getCode())
                .discountType(promo.getDiscountType())
                .discountValue(promo.getDiscountValue())
                .minSpend(promo.getMinSpend())
                .maxDiscount(promo.getMaxDiscount())
                .usageLimit(promo.getUsageLimit())
                .usedCount(promo.getUsedCount())
                .startDate(promo.getStartDate())
                .endDate(promo.getEndDate())
                .isActive(promo.getIsActive())
                .createdAt(promo.getCreatedAt())
                .build();
    }
}
