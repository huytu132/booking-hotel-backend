package com.example.identity_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private Integer hotelId;
    private String hotelName;
    private Integer totalBookings;
    private Integer totalRooms;
    private BigDecimal totalRevenue;
}