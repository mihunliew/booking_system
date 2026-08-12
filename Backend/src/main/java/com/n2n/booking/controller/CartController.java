package com.n2n.booking.controller;

import com.n2n.booking.dto.CartDTOs;
import com.n2n.booking.security.UserPrincipal;
import com.n2n.booking.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartDTOs.CartItemResponse>> getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(cartService.getCart(currentUser.getId()));
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTOs.CartItemResponse> addToCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CartDTOs.AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(currentUser.getId(), request));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartDTOs.CartItemResponse> updateCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody CartDTOs.UpdateCartRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(currentUser.getId(), id, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable(name = "id") Long id) {
        cartService.removeCartItem(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
