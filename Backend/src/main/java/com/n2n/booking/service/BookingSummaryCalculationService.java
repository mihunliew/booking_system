package com.n2n.booking.service;

import com.n2n.booking.dto.BookingSummaryDTOs;
import com.n2n.booking.entity.CartItem;
import com.n2n.booking.entity.PromoCode;
import com.n2n.booking.entity.PromoCodeReservation;
import com.n2n.booking.entity.User;
import com.n2n.booking.enums.DiscountType;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.CartItemRepository;
import com.n2n.booking.repository.PromoCodeRepository;
import com.n2n.booking.repository.PromoCodeReservationRepository;
import com.n2n.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingSummaryCalculationService {

    private final CartItemRepository cartItemRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final com.n2n.booking.repository.ProductSlotHoldRepository productSlotHoldRepository;
    private final com.n2n.booking.repository.BookingItemRepository bookingItemRepository;

    @Transactional
    public BookingSummaryDTOs.BookingSummaryResponse calculateAndApplyPromo(Long userId, String codeInput) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<BookingSummaryDTOs.SummaryItemResponse> itemDTOs = cartItems.stream()
                .map(this::mapToSummaryItem)
                .collect(Collectors.toList());

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // If code is null, blank or user clicked remove -> release existing reservation and return 0 discount summary
        if (codeInput == null || codeInput.trim().isEmpty()) {
            reservationRepository.deleteByUserId(userId);
            return BookingSummaryDTOs.BookingSummaryResponse.builder()
                    .valid(true)
                    .message("Promo code removed")
                    .promoCode(null)
                    .reservationToken(null)
                    .subtotal(subtotal)
                    .discountAmount(BigDecimal.ZERO)
                    .totalAmount(subtotal)
                    .items(itemDTOs)
                    .build();
        }

        String cleanCode = codeInput.trim().toUpperCase();
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(cleanCode).orElse(null);

        // Fail Case helper lambda
        BookingSummaryDTOs.BookingSummaryResponse.BookingSummaryResponseBuilder failResponseBuilder = BookingSummaryDTOs.BookingSummaryResponse.builder()
                .valid(false)
                .promoCode(cleanCode)
                .subtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(subtotal)
                .items(itemDTOs);

        if (promo == null || !Boolean.TRUE.equals(promo.getIsActive())) {
            reservationRepository.deleteByUserId(userId);
            return failResponseBuilder.message("Invalid or inactive promo code").build();
        }

        LocalDateTime now = LocalDateTime.now();
        if (promo.getStartDate() != null && now.isBefore(promo.getStartDate())) {
            reservationRepository.deleteByUserId(userId);
            return failResponseBuilder.message("Promo code is not active yet").build();
        }
        if (promo.getEndDate() != null && now.isAfter(promo.getEndDate())) {
            reservationRepository.deleteByUserId(userId);
            return failResponseBuilder.message("Promo code has expired").build();
        }

        if (promo.getMinSpend() != null && subtotal.compareTo(promo.getMinSpend()) < 0) {
            reservationRepository.deleteByUserId(userId);
            return failResponseBuilder.message("Minimum spend of $" + promo.getMinSpend() + " required for this promo code").build();
        }

        // Usage limit check (usedCount + activeReservations)
        if (promo.getUsageLimit() != null) {
            long activeReservations = reservationRepository.countActiveReservationsByPromoCodeId(promo.getId(), now);
            
            // If the user already has a reservation for THIS promo, exclude it from active count check
            PromoCodeReservation userExistingRes = reservationRepository.findByUserId(userId).orElse(null);
            if (userExistingRes != null && userExistingRes.getPromoCode().getId().equals(promo.getId()) && userExistingRes.getExpiresAt().isAfter(now)) {
                activeReservations--;
            }

            if ((promo.getUsedCount() + activeReservations) >= promo.getUsageLimit()) {
                reservationRepository.deleteByUserId(userId);
                return failResponseBuilder.message("Promo code usage limit reached").build();
            }
        }

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(promo.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (promo.getMaxDiscount() != null && discount.compareTo(promo.getMaxDiscount()) > 0) {
                discount = promo.getMaxDiscount();
            }
        } else if (promo.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = promo.getDiscountValue();
        }

        if (discount.compareTo(subtotal) > 0) {
            discount = subtotal;
        }

        BigDecimal totalAmount = subtotal.subtract(discount);

        // Delete previous reservation for this user and create a new 5-minute reservation
        reservationRepository.deleteByUserId(userId);

        String newToken = UUID.randomUUID().toString();
        PromoCodeReservation newReservation = PromoCodeReservation.builder()
                .reservationToken(newToken)
                .promoCode(promo)
                .user(user)
                .discountAmount(discount)
                .expiresAt(now.plusMinutes(5))
                .build();
        reservationRepository.save(newReservation);

        return BookingSummaryDTOs.BookingSummaryResponse.builder()
                .valid(true)
                .message("Promo code applied successfully (reserved for 5 minutes)")
                .promoCode(promo.getCode())
                .reservationToken(newToken)
                .discountType(promo.getDiscountType())
                .discountValue(promo.getDiscountValue())
                .subtotal(subtotal)
                .discountAmount(discount)
                .totalAmount(totalAmount)
                .items(itemDTOs)
                .build();
    }

    @Transactional
    public BookingSummaryDTOs.BookingSummaryResponse getCheckoutSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        // Check availability and lock slot holds for 5 minutes during GET checkout
        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            java.time.LocalDate bookingDate = cartItem.getBookingDate();
            int stockQty = cartItem.getProduct().getStockQuantity() != null ? cartItem.getProduct().getStockQuantity() : 10;

            int bookedCount = bookingItemRepository.sumConfirmedBookedQuantity(productId, bookingDate);
            int heldCount = productSlotHoldRepository.sumActiveHeldQuantityExcludingUser(productId, bookingDate, userId, LocalDateTime.now());
            int availableUnits = stockQty - (bookedCount + heldCount);

            if (availableUnits < cartItem.getQuantity()) {
                throw new BadRequestException("Sorry, only " + Math.max(0, availableUnits) + " unit(s) of " + cartItem.getProduct().getName() + " are available for " + bookingDate + ". Please adjust your cart.");
            }

            // Create/update 5-minute slot hold for user
            com.n2n.booking.entity.ProductSlotHold hold = productSlotHoldRepository
                    .findByUserIdAndProductIdAndBookingDate(userId, productId, bookingDate)
                    .orElse(com.n2n.booking.entity.ProductSlotHold.builder()
                            .user(user)
                            .product(cartItem.getProduct())
                            .bookingDate(bookingDate)
                            .build());
            hold.setQuantity(cartItem.getQuantity());
            hold.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            productSlotHoldRepository.saveAndFlush(hold);
        }

        List<BookingSummaryDTOs.SummaryItemResponse> itemDTOs = cartItems.stream()
                .map(this::mapToSummaryItem)
                .collect(Collectors.toList());

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check if user has an existing active reservation
        PromoCodeReservation activeRes = reservationRepository.findByUserId(userId)
                .filter(res -> res.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(null);

        if (activeRes != null) {
            PromoCode promo = activeRes.getPromoCode();
            BigDecimal discount = activeRes.getDiscountAmount();
            BigDecimal totalAmount = subtotal.subtract(discount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

            return BookingSummaryDTOs.BookingSummaryResponse.builder()
                    .valid(true)
                    .message("Checkout summary with reserved promo")
                    .promoCode(promo.getCode())
                    .reservationToken(activeRes.getReservationToken())
                    .discountType(promo.getDiscountType())
                    .discountValue(promo.getDiscountValue())
                    .subtotal(subtotal)
                    .discountAmount(discount)
                    .totalAmount(totalAmount)
                    .items(itemDTOs)
                    .build();
        }

        return BookingSummaryDTOs.BookingSummaryResponse.builder()
                .valid(true)
                .message("Checkout summary")
                .promoCode(null)
                .reservationToken(null)
                .subtotal(subtotal)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(subtotal)
                .items(itemDTOs)
                .build();
    }

    private BookingSummaryDTOs.SummaryItemResponse mapToSummaryItem(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        return BookingSummaryDTOs.SummaryItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productCategory(item.getProduct().getCategory())
                .imageUrl(item.getProduct().getImageUrl())
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .bookingDate(item.getBookingDate())
                .subtotal(subtotal)
                .build();
    }
}
