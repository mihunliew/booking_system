package com.n2n.booking.service;

import com.n2n.booking.dto.ProductDTO;
import com.n2n.booking.entity.Product;
import com.n2n.booking.exception.ResourceNotFoundException;
import com.n2n.booking.repository.BookingItemRepository;
import com.n2n.booking.repository.ProductRepository;
import com.n2n.booking.repository.ProductSlotHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ProductSlotHoldRepository productSlotHoldRepository;

    public List<ProductDTO> getAllProducts(String category) {
        List<Product> products;
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("all")) {
            products = productRepository.findByCategoryIgnoreCase(category);
        } else {
            products = productRepository.findAll();
        }
        return products.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductAvailabilityResponse getAvailability(Long productId, java.time.LocalDate bookingDate,
            Long currentUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int stockQty = product.getStockQuantity() != null ? product.getStockQuantity() : 10;
        int bookedCount = bookingItemRepository.sumConfirmedBookedQuantity(productId, bookingDate);
        int heldCount = productSlotHoldRepository.sumActiveHeldQuantityExcludingUser(productId, bookingDate,
                currentUserId, java.time.LocalDateTime.now());

        int availableSlots = stockQty - (bookedCount + heldCount);
        if (availableSlots < 0) {
            availableSlots = 0;
        }

        return ProductDTO.ProductAvailabilityResponse.builder()
                .productId(productId)
                .bookingDate(bookingDate)
                .capacity(product.getCapacity())
                .stockQuantity(stockQty)
                .bookedCount(bookedCount)
                .heldCount(heldCount)
                .availableSlots(availableSlots)
                .build();
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .category(productDTO.getCategory())
                .capacity(productDTO.getCapacity() != null ? productDTO.getCapacity() : 1)
                .stockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 10)
                .imageUrl(productDTO.getImageUrl())
                .status(productDTO.getStatus() != null ? productDTO.getStatus() : "AVAILABLE")
                .build();

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        if (productDTO.getCapacity() != null) {
            product.setCapacity(productDTO.getCapacity());
        }
        if (productDTO.getStockQuantity() != null) {
            product.setStockQuantity(productDTO.getStockQuantity());
        }
        if (productDTO.getImageUrl() != null) {
            product.setImageUrl(productDTO.getImageUrl());
        }
        if (productDTO.getStatus() != null) {
            product.setStatus(productDTO.getStatus());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .capacity(product.getCapacity())
                .stockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 10)
                .imageUrl(product.getImageUrl())
                .status(product.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public ProductDTO.ProductMonthlyScheduleResponse getMonthlySchedule(Long productId, int year, int month) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int stockQty = product.getStockQuantity() != null ? product.getStockQuantity() : 10;
        java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        java.util.List<ProductDTO.DayScheduleDTO> dayScheduleList = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (int day = 1; day <= daysInMonth; day++) {
            java.time.LocalDate date = yearMonth.atDay(day);
            int bookedCount = bookingItemRepository.sumConfirmedBookedQuantity(productId, date);
            int heldCount = productSlotHoldRepository.sumActiveHeldQuantityExcludingUser(productId, date, null, now);
            int availableSlots = stockQty - (bookedCount + heldCount);
            if (availableSlots < 0) {
                availableSlots = 0;
            }

            dayScheduleList.add(ProductDTO.DayScheduleDTO.builder()
                    .date(date)
                    .stockQuantity(stockQty)
                    .bookedCount(bookedCount)
                    .heldCount(heldCount)
                    .availableSlots(availableSlots)
                    .isSoldOut(availableSlots <= 0)
                    .build());
        }

        return ProductDTO.ProductMonthlyScheduleResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .year(year)
                .month(month)
                .totalStockQuantity(stockQty)
                .days(dayScheduleList)
                .build();
    }
}
