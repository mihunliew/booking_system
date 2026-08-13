package com.n2n.booking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Integer stockQuantity;

    private String imageUrl;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductAvailabilityResponse {
        private Long productId;
        private java.time.LocalDate bookingDate;
        private Integer capacity;
        private Integer stockQuantity;
        private Integer bookedCount;
        private Integer heldCount;
        private Integer availableSlots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductMonthlyScheduleResponse {
        private Long productId;
        private String productName;
        private int year;
        private int month;
        private int totalStockQuantity;
        private java.util.List<DayScheduleDTO> days;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayScheduleDTO {
        private java.time.LocalDate date;
        private int stockQuantity;
        private int bookedCount;
        private int heldCount;
        private int availableSlots;
        @com.fasterxml.jackson.annotation.JsonProperty("isSoldOut")
        private boolean isSoldOut;
    }
}
