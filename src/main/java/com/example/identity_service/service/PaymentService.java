package com.example.identity_service.service;

import com.example.identity_service.dto.response.PaymentResponse;
import com.example.identity_service.entity.Booking;
import com.example.identity_service.entity.Payment;
import com.example.identity_service.enums.BookingStatus;
import com.example.identity_service.enums.PaymentStatus;
import com.example.identity_service.enums.PaymentType;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.PaymentMapper;
import com.example.identity_service.repository.BookingRepository;
import com.example.identity_service.repository.PaymentRepository;
import com.example.identity_service.service.cache.BookingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final BookingCacheService bookingCacheService;

    @Transactional
    public PaymentResponse processVNPayResponse(String orderId, String responseCode, String transactionNo, long amount, boolean isReturn) {
        // Extract booking ID from orderId (assuming format "BOOKING-{bookingId}-{timestamp}")
        String bookingIdStr = orderId.split("-")[0].replace("BOOKING-", "");
        Integer bookingId;
        try {
            bookingId = Integer.parseInt(bookingIdStr);
        } catch (NumberFormatException e) {
            log.error("Invalid orderId format: {}", orderId);
            throw new IllegalArgumentException("Invalid orderId format");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));

        // Find or create payment record
        String transId = "PAY-" + orderId;
        Payment payment = paymentRepository.findByBookingAndTransId(booking, transId)
                .orElseGet(() -> createPendingPayment(booking, amount, transId));

        // Check if payment is already processed
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("Payment for booking {} already processed with status {}", bookingId, payment.getPaymentStatus());
            return paymentMapper.toResponse(payment);
        }

        // Validate amount
        long expectedAmount = payment.getPaymentAmount().longValue();
        if (expectedAmount != amount) {
            log.error("Amount mismatch for booking {}. Expected: {}, Received: {}", bookingId, expectedAmount, amount);
            throw new IllegalArgumentException("Amount mismatch");
        }

        // Process based on response code
        if ("00".equals(responseCode)) {
            payment.setPaymentStatus(PaymentStatus.PAID);
            payment.setTransId(transactionNo);
            payment.setPaymentDate(LocalDateTime.now());
            handlePaymentSuccess(bookingId);
            log.info("Payment successful for booking {}. Transaction ID: {}", bookingId, transactionNo);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setTransId(transactionNo);
            handlePaymentFailure(bookingId);
            log.warn("Payment failed for booking {}. Response code: {}", bookingId, responseCode);
        }

        paymentRepository.save(payment);
        bookingRepository.save(booking);

        return paymentMapper.toResponse(payment);
    }

    private Payment createPendingPayment(Booking booking, long amount, String transId) {
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentAmount(new BigDecimal(amount))
                .paymentType(PaymentType.ONLINE_PAYMENT)
                .paymentDate(LocalDateTime.now())
                .transId(transId)
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public void handlePaymentSuccess(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_STATE);
        }

        // Cập nhật DB
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.getBookingRoomClasses().forEach(brc -> brc.setStatus("CONFIRMED"));
        // lưu transactionId nếu cần
        bookingRepository.save(booking);

        // Cleanup hold trong Redis
        bookingCacheService.releaseHoldsForBooking(bookingId);
    }


    @Transactional
    public void handlePaymentFailure(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getBookingStatus() == BookingStatus.PENDING) {
            booking.setBookingStatus(BookingStatus.CART);
            booking.getBookingRoomClasses().forEach(brc -> brc.setStatus("IN_CART"));
            bookingRepository.save(booking);
        }

        // Bỏ hold để người khác đặt được ngay
        bookingCacheService.releaseHoldsForBooking(bookingId);
    }

}