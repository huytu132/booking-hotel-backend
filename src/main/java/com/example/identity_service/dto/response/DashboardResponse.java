package com.example.identity_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    // Today's statistics
    private Integer todayCheckIns;
    private Integer todayCheckOuts;
    private Integer todayNewBookings;
    private BigDecimal todayRevenue;

    // This month statistics
    private Integer monthlyBookings;
    private BigDecimal monthlyRevenue;
    private Double monthlyOccupancyRate;

    // Pending actions
    private Integer pendingConfirmations;
    private Integer upcomingCheckIns;
    private Integer overdueCheckIns;

    // Charts data
    private List<Map<String, Object>> revenueChart; // 7 days revenue
    private List<Map<String, Object>> bookingChart; // 7 days bookings
    private Map<String, Integer> roomStatusSummary; // Available, Occupied, etc.
}