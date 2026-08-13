package com.n2n.booking.controller;

import com.n2n.booking.dto.ProductDTO;
import com.n2n.booking.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(@RequestParam(name = "category", required = false) String category) {
        return ResponseEntity.ok(productService.getAllProducts(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ProductDTO.ProductAvailabilityResponse> getProductAvailability(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "date") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.n2n.booking.entity.User currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(productService.getAvailability(id, date, userId));
    }
}
