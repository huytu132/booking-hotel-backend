//package com.example.identity_service.service;
//
//import com.example.identity_service.dto.response.DashboardResponse;
//import com.example.identity_service.entity.Booking;
//import com.example.identity_service.entity.Room;
//import com.example.identity_service.enums.BookingStatus;
//import com.example.identity_service.enums.RoomStatusType;
//import com.example.identity_service.repository.BookingRepository;
//import com.example.identity_service.repository.RoomRepository;
//import com.example.identity_service.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class AdminDashboardService {
//
//    private final BookingRepository bookingRepository;
//    private final RoomRepository roomRepository;
//    private final PaymentRepository paymentRepository;
//
//    public DashboardResponse getDashboardData() {
//        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
//        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
//        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
//
//        // Today's statistics
//        Integer todayCheckIns = bookingRepository.countTodayCheckIns(todayStart, todayEnd);
//        Integer todayCheckOuts = bookingRepository.countTodayCheckOuts(todayStart, todayEnd);
//        Integer todayNewBookings = bookingRepository.countByBookingStatusAndCreateAtBetween(
//                BookingStatus.PENDING, todayStart, todayEnd);
//        BigDecimal todayRevenue = paymentRepository.getTodayRevenue(todayStart, todayEnd);
//
//        // Monthly statistics
//        Integer monthlyBookings = bookingRepository.countByCreateAtBetween(monthStart, todayEnd);
//        BigDecimal monthlyRevenue = paymentRepository.getMonthlyRevenue(monthStart, todayEnd);
//
//        // Occupancy rate
//        long totalRooms = roomRepository.count();
//        long occupiedRooms = roomRepository.countByRoomStatus(RoomStatusType.OCCUPIED);
//        Double monthlyOccupancyRate = totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0.0;
//
//        // Pending actions - FIX: Chuyển long thành Integer trực tiếp
//        Integer pendingConfirmations = (int) bookingRepository.countByBookingStatus(BookingStatus.PENDING);
//        Integer upcomingCheckIns = bookingRepository.countUpcomingCheckIns(
//                BookingStatus.CONFIRMED, todayStart, todayEnd.plusDays(1));
//        Integer overdueCheckIns = bookingRepository.countOverdueCheckIns(
//                BookingStatus.CONFIRMED, todayStart.minusDays(1));
//
//        // Revenue chart (last 7 days)
//        List<Map<String, Object>> revenueChart = new ArrayList<>();
//        for (int i = 6; i >= 0; i--) {
//            LocalDate date = LocalDate.now().minusDays(i);
//            BigDecimal revenue = paymentRepository.getRevenueByDate(
//                    date.atStartOfDay(), date.atTime(23, 59, 59));
//
//            Map<String, Object> point = new HashMap<>();
//            point.put("date", date.toString());
//            point.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
//            revenueChart.add(point);
//        }
//
//        // Booking chart (last 7 days)
//        List<Map<String, Object>> bookingChart = new ArrayList<>();
//        for (int i = 6; i >= 0; i--) {
//            LocalDate date = LocalDate.now().minusDays(i);
//            Integer bookings = bookingRepository.countByCreateAtBetween(
//                    date.atStartOfDay(), date.atTime(23, 59, 59));
//
//            Map<String, Object> point = new HashMap<>();
//            point.put("date", date.toString());
//            point.put("bookings", bookings != null ? bookings : 0);
//            bookingChart.add(point);
//        }
//
//        // Room status summary - FIX: Chuyển long thành Integer
//        Map<String, Integer> roomStatusSummary = new HashMap<>();
//        for (RoomStatusType status : RoomStatusType.values()) {
//            Long count = roomRepository.countByRoomStatus(status);
//            roomStatusSummary.put(status.name(), count != null ? count.intValue() : 0);
//        }
//
//        return DashboardResponse.builder()
//                .todayCheckIns(todayCheckIns != null ? todayCheckIns : 0)
//                .todayCheckOuts(todayCheckOuts != null ? todayCheckOuts : 0)
//                .todayNewBookings(todayNewBookings != null ? todayNewBookings : 0)
//                .todayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO)
//                .monthlyBookings(monthlyBookings != null ? monthlyBookings : 0)
//                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
//                .monthlyOccupancyRate(monthlyOccupancyRate)
//                .pendingConfirmations(pendingConfirmations)
//                .upcomingCheckIns(upcomingCheckIns != null ? upcomingCheckIns : 0)
//                .overdueCheckIns(overdueCheckIns != null ? overdueCheckIns : 0)
//                .revenueChart(revenueChart)
//                .bookingChart(bookingChart)
//                .roomStatusSummary(roomStatusSummary)
//                .build();
//    }
//}