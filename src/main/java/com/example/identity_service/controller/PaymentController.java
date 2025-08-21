package com.example.identity_service.controller;

import com.example.identity_service.dto.request.PaymentRequest;
import com.example.identity_service.dto.response.PaymentResponse;
import com.example.identity_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // User tạo payment cho booking của mình
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }

    // Lấy lịch sử thanh toán của booking
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(bookingId));
    }

    // Lấy tổng quan thanh toán
    @GetMapping("/booking/{bookingId}/summary")
    public ResponseEntity<Map<String, Object>> getPaymentSummary(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentSummary(bookingId));
    }

    // === ADMIN ENDPOINTS ===

    // Admin xác nhận thanh toán
    @PutMapping("/{paymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Integer paymentId,
            @RequestParam String transactionId) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentId, transactionId));
    }

    // Admin hủy thanh toán
    @PutMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Integer paymentId) {
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId));
    }

    // Admin hoàn tiền
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refundPayment(
            @RequestParam Integer bookingId,
            @RequestParam BigDecimal amount,
            @RequestParam String reason) {
        return ResponseEntity.ok(paymentService.refundPayment(bookingId, amount, reason));
    }
}