package com.n2n.booking.service;

import com.n2n.booking.dto.CartDTOs;
import com.n2n.booking.entity.CartItem;
import com.n2n.booking.entity.Product;
import com.n2n.booking.entity.User;
import com.n2n.booking.exception.BadRequestException;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.BookingItemRepository;
import com.n2n.booking.repository.CartItemRepository;
import com.n2n.booking.repository.ProductRepository;
import com.n2n.booking.repository.ProductSlotHoldRepository;
import com.n2n.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ProductSlotHoldRepository productSlotHoldRepository;

    public List<CartDTOs.CartItemResponse> getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CartDTOs.CartItemResponse addToCart(Long userId, CartDTOs.AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductIdAndBookingDate(
                userId, request.getProductId(), request.getBookingDate());

        int currentQtyInCart = existingItem.map(CartItem::getQuantity).orElse(0);
        int targetQty = currentQtyInCart + request.getQuantity();

        // Check availability
        validateAvailability(product, request.getBookingDate(), targetQty, userId);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(targetQty);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity())
                    .bookingDate(request.getBookingDate())
                    .build();
        }

        CartItem saved = cartItemRepository.save(cartItem);
        return mapToDTO(saved);
    }

    @Transactional
    public CartDTOs.CartItemResponse updateCartItem(Long userId, Long itemId, CartDTOs.UpdateCartRequest request) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify this cart item");
        }

        // Check availability for updated date & quantity
        validateAvailability(cartItem.getProduct(), request.getBookingDate(), request.getQuantity(), userId);

        cartItem.setQuantity(request.getQuantity());
        cartItem.setBookingDate(request.getBookingDate());

        CartItem updated = cartItemRepository.save(cartItem);
        return mapToDTO(updated);
    }

    private void validateAvailability(Product product, LocalDate bookingDate, int requiredQty, Long userId) {
        com.n2n.booking.util.ProductValidationUtil.validateProductAvailability(product);
        int stockQty = product.getStockQuantity() != null ? product.getStockQuantity() : 10;
        int bookedCount = bookingItemRepository.sumConfirmedBookedQuantity(product.getId(), bookingDate);
        int heldCount = productSlotHoldRepository.sumActiveHeldQuantityExcludingUser(product.getId(), bookingDate, userId, LocalDateTime.now());
        int availableUnits = stockQty - (bookedCount + heldCount);

        if (availableUnits < requiredQty) {
            throw new BadRequestException("Sorry, only " + Math.max(0, availableUnits) + " unit(s) of " + product.getName() + " are available for " + bookingDate);
        }
    }

    @Transactional
    public void removeCartItem(Long userId, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartDTOs.CartItemResponse mapToDTO(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getPrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartDTOs.CartItemResponse.builder()
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
