package com.n2n.booking.controller;

import com.n2n.booking.dto.ProductDTO;
import com.n2n.booking.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PRODUCTS_READ')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllAdminProducts() {
        return ResponseEntity.ok(productService.getAllProducts(null));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PRODUCTS_CREATE')")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.createProduct(productDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PRODUCTS_UPDATE')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'PRODUCTS_DELETE')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ProductDTO.ProductMonthlyScheduleResponse> getMonthlySchedule(
            @PathVariable Long id,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(productService.getMonthlySchedule(id, year, month));
    }
}
