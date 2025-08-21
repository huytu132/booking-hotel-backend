package com.example.identity_service.service;

import com.example.identity_service.dto.request.PaymentRequest;
import com.example.identity_service.dto.response.PaymentResponse;
import com.example.identity_service.entity.Booking;
import com.example.identity_service.entity.Payment;
import com.example.identity_service.enums.BookingStatus;
import com.example.identity_service.enums.PaymentStatus;
import com.example.identity_service.mapper.PaymentMapper;
import com.example.identity_service.repository.BookingRepository;
import com.example.identity_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;

    // Tạo payment cho booking
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Kiểm tra booking đã được confirm chưa
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED &&
                booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new RuntimeException("Booking must be confirmed before payment");
        }

        // Kiểm tra đã thanh toán chưa
        List<Payment> existingPayments = paymentRepository.findByBooking(booking);
        BigDecimal totalPaid = existingPayments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(booking.getBookingAmount()) >= 0) {
            throw new RuntimeException("Booking has already been fully paid");
        }

        // Tạo payment
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentAmount(request.getAmount())
                .paymentType(request.getPaymentType())
                .paymentDate(LocalDateTime.now())
                .transId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    // Xác nhận thanh toán
    @Transactional
    public PaymentResponse confirmPayment(Integer paymentId, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment is not in pending status");
        }

        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setTransId(transactionId);
        payment.setPaymentDate(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment {} confirmed with transaction ID: {}", paymentId, transactionId);

        return paymentMapper.toResponse(savedPayment);
    }

    // Hủy thanh toán
    @Transactional
    public PaymentResponse cancelPayment(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Only pending payments can be cancelled");
        }

        payment.setPaymentStatus(PaymentStatus.FAILED);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment {} cancelled", paymentId);

        return paymentMapper.toResponse(savedPayment);
    }

    // Hoàn tiền
    @Transactional
    public PaymentResponse refundPayment(Integer bookingId, BigDecimal amount, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Kiểm tra tổng tiền đã thanh toán
        List<Payment> payments = paymentRepository.findByBooking(booking);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunded = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.REFUNDED)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (amount.compareTo(totalPaid.subtract(totalRefunded)) > 0) {
            throw new RuntimeException("Refund amount exceeds paid amount");
        }

        // Tạo refund record
        Payment refund = Payment.builder()
                .booking(booking)
                .paymentStatus(PaymentStatus.REFUNDED)
                .paymentAmount(amount.negate()) // Số tiền âm cho refund
                .paymentType(payments.get(0).getPaymentType())
                .paymentDate(LocalDateTime.now())
                .transId("REFUND-" + UUID.randomUUID().toString().substring(0, 8))
                .build();

        Payment savedRefund = paymentRepository.save(refund);

        log.info("Refund created for booking {}: {} - Reason: {}", bookingId, amount, reason);

        return paymentMapper.toResponse(savedRefund);
    }

    // Lấy lịch sử thanh toán của booking
    public List<PaymentResponse> getPaymentHistory(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        List<Payment> payments = paymentRepository.findByBookingOrderByPaymentDateDesc(booking);

        return payments.stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy tổng quan thanh toán của booking
    public Map<String, Object> getPaymentSummary(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        List<Payment> payments = paymentRepository.findByBooking(booking);

        BigDecimal totalAmount = booking.getBookingAmount();
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRefunded = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.REFUNDED)
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs();
        BigDecimal balance = totalAmount.subtract(totalPaid).add(totalRefunded);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAmount", totalAmount);
        summary.put("totalPaid", totalPaid);
        summary.put("totalRefunded", totalRefunded);
        summary.put("balance", balance);
        summary.put("isPaid", balance.compareTo(BigDecimal.ZERO) <= 0);

        return summary;
    }
}