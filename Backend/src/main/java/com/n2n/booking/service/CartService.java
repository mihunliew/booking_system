package com.n2n.booking.service;

import com.n2n.booking.dto.CartDTOs;
import com.n2n.booking.entity.CartItem;
import com.n2n.booking.entity.Product;
import com.n2n.booking.entity.User;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.CartItemRepository;
import com.n2n.booking.repository.ProductRepository;
import com.n2n.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

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

        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
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

        cartItem.setQuantity(request.getQuantity());
        cartItem.setBookingDate(request.getBookingDate());

        CartItem updated = cartItemRepository.save(cartItem);
        return mapToDTO(updated);
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
