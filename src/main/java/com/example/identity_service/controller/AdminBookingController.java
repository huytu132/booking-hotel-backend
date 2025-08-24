//package com.example.identity_service.controller;
//
//import com.example.identity_service.dto.request.BookingSearchRequest;
//import com.example.identity_service.dto.request.BookingStatusUpdateRequest;
//import com.example.identity_service.dto.response.BookingResponse;
//import com.example.identity_service.dto.response.BookingStatisticsResponse;
//import com.example.identity_service.dto.response.RevenueReportResponse;
//import com.example.identity_service.service.AdminBookingService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.web.PageableDefault;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/admin/bookings")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminBookingController {
//
//    private final AdminBookingService adminBookingService;
//
//    // === BOOKING MANAGEMENT ===
//
//    // Lấy danh sách booking với filter và phân trang
//    @GetMapping
//    public ResponseEntity<Page<BookingResponse>> getAllBookings(
//            @ModelAttribute BookingSearchRequest searchRequest,
//            @PageableDefault(size = 20, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
//        return ResponseEntity.ok(adminBookingService.getAllBookings(searchRequest, pageable));
//    }
//
//    // Xem chi tiết booking
//    @GetMapping("/{bookingId}")
//    public ResponseEntity<BookingResponse> getBookingDetail(@PathVariable Integer bookingId) {
//        return ResponseEntity.ok(adminBookingService.getBookingDetail(bookingId));
//    }
//
//    // Cập nhật trạng thái booking
//    @PutMapping("/{bookingId}/status")
//    public ResponseEntity<BookingResponse> updateBookingStatus(
//            @PathVariable Integer bookingId,
//            @RequestBody BookingStatusUpdateRequest request) {
//        return ResponseEntity.ok(adminBookingService.updateBookingStatus(bookingId, request));
//    }
//
//    // Xác nhận booking
//    @PutMapping("/{bookingId}/confirm")
//    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Integer bookingId) {
//        return ResponseEntity.ok(adminBookingService.confirmBooking(bookingId));
//    }
//
//    // Check-in
//    @PutMapping("/{bookingId}/check-in")
//    public ResponseEntity<BookingResponse> checkIn(@PathVariable Integer bookingId) {
//        return ResponseEntity.ok(adminBookingService.checkIn(bookingId));
//    }
//
//    // Check-out
//    @PutMapping("/{bookingId}/check-out")
//    public ResponseEntity<BookingResponse> checkOut(@PathVariable Integer bookingId) {
//        return ResponseEntity.ok(adminBookingService.checkOut(bookingId));
//    }
//
//    // Hủy booking
//    @PutMapping("/{bookingId}/cancel")
//    public ResponseEntity<BookingResponse> cancelBooking(
//            @PathVariable Integer bookingId,
//            @RequestParam String reason) {
//        return ResponseEntity.ok(adminBookingService.cancelBooking(bookingId, reason));
//    }
//
//    // === REPORTS & STATISTICS ===
//
//    // Thống kê booking
//    @GetMapping("/statistics")
//    public ResponseEntity<BookingStatisticsResponse> getBookingStatistics(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
//        return ResponseEntity.ok(adminBookingService.getBookingStatistics(fromDate, toDate));
//    }
//
//    // Báo cáo doanh thu theo khách sạn
//    @GetMapping("/revenue/by-hotel")
//    public ResponseEntity<List<RevenueReportResponse>> getRevenueByHotel(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
//        return ResponseEntity.ok(adminBookingService.getRevenueByHotel(fromDate, toDate));
//    }
//
//    // === OPERATIONAL FEATURES ===
//
//    // Lấy danh sách booking sắp check-in (24h tới)
//    @GetMapping("/upcoming-checkins")
//    public ResponseEntity<List<BookingResponse>> getUpcomingCheckIns() {
//        return ResponseEntity.ok(adminBookingService.getUpcomingCheckIns());
//    }
//
//    // Đánh dấu booking no-show
//    @PostMapping("/mark-no-show")
//    public ResponseEntity<List<BookingResponse>> markNoShowBookings() {
//        return ResponseEntity.ok(adminBookingService.markNoShowBookings());
//    }
//
//    // Export bookings data (để frontend xử lý Excel)
////    @GetMapping("/export")
////    public ResponseEntity<List<Map<String, Object>>> exportBookings(
////            @ModelAttribute BookingSearchRequest searchRequest) {
////        return ResponseEntity.ok(adminBookingService.exportBookingsData(searchRequest));
////    }
//}