//package com.example.identity_service.service;
//
//import com.example.identity_service.dto.request.BookingStatusUpdateRequest;
//import com.example.identity_service.dto.request.BookingSearchRequest;
//import com.example.identity_service.dto.response.BookingResponse;
//import com.example.identity_service.dto.response.BookingStatisticsResponse;
//import com.example.identity_service.dto.response.RevenueReportResponse;
//import com.example.identity_service.entity.Booking;
//import com.example.identity_service.entity.BookingRoomClass;
//import com.example.identity_service.entity.Payment;
//import com.example.identity_service.enums.BookingStatus;
//import com.example.identity_service.enums.PaymentStatus;
//import com.example.identity_service.enums.PaymentType;
//import com.example.identity_service.mapper.BookingMapper;
//import com.example.identity_service.repository.BookingRepository;
//import com.example.identity_service.repository.BookingRoomClassRepository;
//import com.example.identity_service.repository.PaymentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import jakarta.persistence.criteria.Predicate;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class AdminBookingService {
//
//    private final BookingRepository bookingRepository;
//    private final BookingRoomClassRepository bookingRoomClassRepository;
//    private final PaymentRepository paymentRepository;
//    private final BookingMapper bookingMapper;
//
//    // 1. Lấy tất cả bookings với filter và phân trang
//    public Page<BookingResponse> getAllBookings(BookingSearchRequest searchRequest, Pageable pageable) {
//        Specification<Booking> spec = buildSearchSpecification(searchRequest);
//        Page<Booking> bookings = bookingRepository.findAll(spec, pageable);
//        return bookings.map(bookingMapper::toResponse);
//    }
//
//    // 2. Lấy chi tiết booking bất kỳ
//    public BookingResponse getBookingDetail(Integer bookingId) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//        return bookingMapper.toResponse(booking);
//    }
//
//    // 3. Cập nhật trạng thái booking
//    @Transactional
//    public BookingResponse updateBookingStatus(Integer bookingId, BookingStatusUpdateRequest request) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        BookingStatus oldStatus = booking.getBookingStatus();
//        BookingStatus newStatus = request.getStatus();
//
//        // Validate status transition
//        validateStatusTransition(oldStatus, newStatus);
//
//        booking.setBookingStatus(newStatus);
//
//        // Cập nhật status cho các booking room
//        String roomStatus = mapBookingStatusToRoomStatus(newStatus);
//        for (BookingRoomClass bookingRoomClass : booking.getBookingRoomClasses()) {
//            bookingRoomClass.setStatus(roomStatus);
//        }
//
//        // Nếu confirm booking, tạo payment record
//        if (newStatus == BookingStatus.CONFIRMED && oldStatus == BookingStatus.PENDING) {
//            createPaymentRecord(booking);
//        }
//
//        // Log status change
//        log.info("Booking {} status changed from {} to {} by admin. Note: {}",
//                bookingId, oldStatus, newStatus, request.getNote());
//
//        Booking savedBooking = bookingRepository.save(booking);
//        return bookingMapper.toResponse(savedBooking);
//    }
//
//    // 4. Xác nhận booking (PENDING -> CONFIRMED)
//    @Transactional
//    public BookingResponse confirmBooking(Integer bookingId) {
//        BookingStatusUpdateRequest request = new BookingStatusUpdateRequest();
//        request.setStatus(BookingStatus.CONFIRMED);
//        request.setNote("Confirmed by admin");
//        return updateBookingStatus(bookingId, request);
//    }
//
//    // 5. Check-in
//    @Transactional
//    public BookingResponse checkIn(Integer bookingId) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
//            throw new RuntimeException("Can only check-in confirmed bookings");
//        }
//
//        // Kiểm tra ngày check-in
//        LocalDateTime now = LocalDateTime.now();
//        boolean hasValidCheckin = booking.getBookingRoomClasses().stream()
//                .anyMatch(room -> {
//                    LocalDateTime checkinDate = room.getCheckinDate();
//                    // Cho phép check-in từ 2h trước
//                    return now.isAfter(checkinDate.minusHours(2)) && now.isBefore(checkinDate.plusDays(1));
//                });
//
//        if (!hasValidCheckin) {
//            throw new RuntimeException("Check-in date is not valid for any room in this booking");
//        }
//
//        booking.setBookingStatus(BookingStatus.CHECKED_IN);
//
//        // Cập nhật room status
//        for (BookingRoomClass bookingRoomClass : booking.getBookingRoomClasses()) {
//            bookingRoomClass.setStatus("CHECKED_IN");
//        }
//
//        Booking savedBooking = bookingRepository.save(booking);
//        return bookingMapper.toResponse(savedBooking);
//    }
//
//    // 6. Check-out
//    @Transactional
//    public BookingResponse checkOut(Integer bookingId) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
//            throw new RuntimeException("Can only check-out checked-in bookings");
//        }
//
//        // Kiểm tra payment đã thanh toán chưa
//        List<Payment> payments = paymentRepository.findByBooking(booking);
//        boolean allPaid = payments.stream()
//                .allMatch(p -> p.getPaymentStatus() == PaymentStatus.PAID);
//
//        if (!allPaid) {
//            throw new RuntimeException("Cannot check-out: Payment not completed");
//        }
//
//        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
//
//        // Cập nhật room status
//        for (BookingRoomClass bookingRoomClass : booking.getBookingRoomClasses()) {
//            bookingRoomClass.setStatus("CHECKED_OUT");
//        }
//
//        Booking savedBooking = bookingRepository.save(booking);
//        return bookingMapper.toResponse(savedBooking);
//    }
//
//    // 7. Hủy booking (Admin có thể hủy ở bất kỳ status nào trừ CHECKED_OUT)
//    @Transactional
//    public BookingResponse cancelBooking(Integer bookingId, String reason) {
//        Booking booking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        if (booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
//            throw new RuntimeException("Cannot cancel checked-out bookings");
//        }
//
//        booking.setBookingStatus(BookingStatus.CANCELLED);
//
//        // Cập nhật room status
//        for (BookingRoomClass bookingRoomClass : booking.getBookingRoomClasses()) {
//            bookingRoomClass.setStatus("CANCELLED");
//        }
//
//        // Nếu đã có payment, tạo refund record
//        List<Payment> payments = paymentRepository.findByBooking(booking);
//        for (Payment payment : payments) {
//            if (payment.getPaymentStatus() == PaymentStatus.PAID) {
//                Payment refund = Payment.builder()
//                        .booking(booking)
//                        .paymentStatus(PaymentStatus.REFUNDED)
//                        .paymentAmount(payment.getPaymentAmount())
//                        .paymentType(payment.getPaymentType())
//                        .paymentDate(LocalDateTime.now())
//                        .transId("REFUND-" + UUID.randomUUID().toString().substring(0, 8))
//                        .build();
//                paymentRepository.save(refund);
//            }
//        }
//
//        log.info("Booking {} cancelled by admin. Reason: {}", bookingId, reason);
//
//        Booking savedBooking = bookingRepository.save(booking);
//        return bookingMapper.toResponse(savedBooking);
//    }
//
//    // 8. Thống kê booking
//    public BookingStatisticsResponse getBookingStatistics(LocalDate fromDate, LocalDate toDate) {
//        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
//        LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : LocalDateTime.now();
//
//        List<Booking> bookings = bookingRepository.findByCreateAtBetween(from, to);
//
//        // Thống kê theo status
//        Map<BookingStatus, Long> statusCount = bookings.stream()
//                .filter(b -> b.getBookingStatus() != BookingStatus.CART)
//                .collect(Collectors.groupingBy(Booking::getBookingStatus, Collectors.counting()));
//
//        // Tính tổng doanh thu
//        BigDecimal totalRevenue = bookings.stream()
//                .filter(b -> b.getBookingStatus() == BookingStatus.CHECKED_OUT)
//                .map(Booking::getBookingAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Tính doanh thu pending
//        BigDecimal pendingRevenue = bookings.stream()
//                .filter(b -> Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN)
//                        .contains(b.getBookingStatus()))
//                .map(Booking::getBookingAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Tính tỷ lệ hủy
//        long totalBookings = bookings.size() - statusCount.getOrDefault(BookingStatus.CART, 0L);
//        long cancelledBookings = statusCount.getOrDefault(BookingStatus.CANCELLED, 0L);
//        double cancellationRate = totalBookings > 0 ? (double) cancelledBookings / totalBookings * 100 : 0;
//
//        return BookingStatisticsResponse.builder()
//                .fromDate(from)
//                .toDate(to)
//                .totalBookings(totalBookings)
//                .statusBreakdown(statusCount)
//                .totalRevenue(totalRevenue)
//                .pendingRevenue(pendingRevenue)
//                .cancellationRate(cancellationRate)
//                .averageBookingValue(totalBookings > 0 ?
//                        totalRevenue.divide(BigDecimal.valueOf(totalBookings), 2, BigDecimal.ROUND_HALF_UP) :
//                        BigDecimal.ZERO)
//                .build();
//    }
//
//    // 9. Báo cáo doanh thu theo khách sạn
//    public List<RevenueReportResponse> getRevenueByHotel(LocalDate fromDate, LocalDate toDate) {
//        LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
//        LocalDateTime to = toDate != null ? toDate.atTime(23, 59, 59) : LocalDateTime.now();
//
//        List<Object[]> results = bookingRepository.getRevenueByHotel(from, to);
//
//        return results.stream().map(row -> RevenueReportResponse.builder()
//                        .hotelId((Integer) row[0])
//                        .hotelName((String) row[1])
//                        .totalBookings(((Long) row[2]).intValue())
//                        .totalRooms(((Long) row[3]).intValue())
//                        .totalRevenue((BigDecimal) row[4])
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    // 10. Lấy danh sách booking sắp check-in (trong 24h tới)
//    public List<BookingResponse> getUpcomingCheckIns() {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime tomorrow = now.plusDays(1);
//
//        List<Booking> bookings = bookingRepository.findUpcomingCheckIns(
//                BookingStatus.CONFIRMED, now, tomorrow);
//
//        return bookings.stream()
//                .map(bookingMapper::toResponse)
//                .collect(Collectors.toList());
//    }
//
//    // 11. Lấy danh sách booking quá hạn check-in (No show)
//    @Transactional
//    public List<BookingResponse> markNoShowBookings() {
//        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
//
//        List<Booking> overdueBookings = bookingRepository.findOverdueCheckIns(
//                BookingStatus.CONFIRMED, yesterday);
//
//        List<BookingResponse> noShowBookings = new ArrayList<>();
//
//        for (Booking booking : overdueBookings) {
//            booking.setBookingStatus(BookingStatus.NO_SHOW);
//            for (BookingRoomClass room : booking.getBookingRoomClasses()) {
//                room.setStatus("NO_SHOW");
//            }
//            Booking saved = bookingRepository.save(booking);
//            noShowBookings.add(bookingMapper.toResponse(saved));
//        }
//
//        log.info("Marked {} bookings as NO_SHOW", noShowBookings.size());
//        return noShowBookings;
//    }
//
//    // 12. Export bookings to Excel (data preparation)
////    public List<Map<String, Object>> exportBookingsData(BookingSearchRequest searchRequest) {
////        Specification<Booking> spec = buildSearchSpecification(searchRequest);
////        List<Booking> bookings = bookingRepository.findAll(spec);
////
////        return bookings.stream().map(booking -> {
////            Map<String, Object> row = new HashMap<>();
////            row.put("Booking ID", booking.getId());
////            row.put("Guest Name", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
////            row.put("Email", booking.getUser().getEmail());
////            row.put("Phone", booking.getUser().getPhoneNo());
////            row.put("Total Rooms", booking.getTotalRoom());
////            row.put("Total Amount", booking.getBookingAmount());
////            row.put("Status", booking.getBookingStatus().toString());
////            row.put("Created Date", booking.getCreateAt());
////
////            // Room details
////            String roomDetails = booking.getBookingRoomClasses().stream()
////                    .map(br -> String.format("%s (%s - %s)",
////                            br.getRoom().getRoomNumber(),
////                            br.getCheckinDate().toLocalDate(),
////                            br.getCheckoutDate().toLocalDate()))
////                    .collect(Collectors.joining(", "));
////            row.put("Rooms", roomDetails);
////
////            return row;
////        }).collect(Collectors.toList());
////    }
//
//    // Helper methods
//    private Specification<Booking> buildSearchSpecification(BookingSearchRequest request) {
//        return (root, query, criteriaBuilder) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Filter by status
//            if (request.getStatus() != null) {
//                predicates.add(criteriaBuilder.equal(root.get("bookingStatus"), request.getStatus()));
//            }
//
//            // Filter by date range
//            if (request.getFromDate() != null) {
//                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
//                        root.get("createAt"), request.getFromDate().atStartOfDay()));
//            }
//            if (request.getToDate() != null) {
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(
//                        root.get("createAt"), request.getToDate().atTime(23, 59, 59)));
//            }
//
//            // Filter by user email
//            if (request.getUserEmail() != null && !request.getUserEmail().isEmpty()) {
//                predicates.add(criteriaBuilder.like(
//                        criteriaBuilder.lower(root.get("user").get("email")),
//                        "%" + request.getUserEmail().toLowerCase() + "%"));
//            }
//
//            // Filter by hotel
//            if (request.getHotelId() != null) {
//                predicates.add(criteriaBuilder.equal(
//                        root.join("bookingRooms").join("room").join("roomClass").join("hotel").get("id"),
//                        request.getHotelId()));
//            }
//
//            // Exclude CART status by default
//            if (!request.isIncludeCart()) {
//                predicates.add(criteriaBuilder.notEqual(root.get("bookingStatus"), BookingStatus.CART));
//            }
//
//            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    private void validateStatusTransition(BookingStatus from, BookingStatus to) {
//        Map<BookingStatus, List<BookingStatus>> allowedTransitions = new HashMap<>();
//        allowedTransitions.put(BookingStatus.PENDING, Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.CANCELLED));
//        allowedTransitions.put(BookingStatus.CONFIRMED, Arrays.asList(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED, BookingStatus.NO_SHOW));
//        allowedTransitions.put(BookingStatus.CHECKED_IN, Arrays.asList(BookingStatus.CHECKED_OUT));
//        allowedTransitions.put(BookingStatus.NO_SHOW, Arrays.asList(BookingStatus.CANCELLED));
//
//        if (!allowedTransitions.getOrDefault(from, Collections.emptyList()).contains(to)) {
//            throw new RuntimeException(String.format("Invalid status transition from %s to %s", from, to));
//        }
//    }
//
//    private String mapBookingStatusToRoomStatus(BookingStatus status) {
//        switch (status) {
//            case PENDING: return "PENDING";
//            case CONFIRMED: return "CONFIRMED";
//            case CHECKED_IN: return "CHECKED_IN";
//            case CHECKED_OUT: return "CHECKED_OUT";
//            case CANCELLED: return "CANCELLED";
//            case NO_SHOW: return "NO_SHOW";
//            default: return status.toString();
//        }
//    }
//
//    private void createPaymentRecord(Booking booking) {
//        Payment payment = Payment.builder()
//                .booking(booking)
//                .paymentStatus(PaymentStatus.PENDING)
//                .paymentAmount(booking.getBookingAmount())
//                .paymentType(PaymentType.BANK_TRANSFER)
//                .paymentDate(LocalDateTime.now())
//                .transId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
//                .build();
//        paymentRepository.save(payment);
//    }
//}