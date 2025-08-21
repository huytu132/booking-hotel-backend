package com.example.identity_service.dto.response;

import com.example.identity_service.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatisticsResponse {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Long totalBookings;
    private Map<BookingStatus, Long> statusBreakdown;
    private BigDecimal totalRevenue;
    private BigDecimal pendingRevenue;
    private Double cancellationRate;
    private BigDecimal averageBookingValue;
}