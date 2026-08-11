package com.n2n.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private Long totalUsers;
    private Long totalProducts;
    private Long totalBookings;
    private Long pendingBookings;
    private BigDecimal totalRevenue;
    private List<BookingDTOs.BookingResponse> recentBookings;
}
