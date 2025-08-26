package com.example.identity_service.controller;

import com.example.identity_service.dto.response.PaymentResponse;
import com.example.identity_service.entity.Payment;
import com.example.identity_service.service.PaymentService;
import com.example.identity_service.service.VNPayService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Integer id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/vnpay-return")
    public void handleVNPayReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        // Validate chữ ký VNPay
        if (!vnPayService.validateResponse(params)) {
            response.sendRedirect("http://localhost:5173.com/payment/result?status=failed&reason=invalid_signature");
            return;
        }

        // Xử lý thông tin VNPay gửi về và cập nhật Payment
        PaymentResponse payment = processVNPayParams(params);

        // Redirect về FE kèm paymentId và status
        String redirectUrl = "http://localhost:5173.com/payment/result"
                + "?paymentId=" + payment.getId();

        response.sendRedirect(redirectUrl);
    }



    @PostMapping("/vnpay-ipn")
    public ResponseEntity<Map<String, String>> handleIPN(@RequestParam Map<String, String> params) {
        log.info("VNPay IPN params: {}", params);
        Map<String, String> response = new HashMap<>();

        if (!vnPayService.validateResponse(params)) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid checksum");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            processVNPayParams(params);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error processing VNPay IPN: {}", e.getMessage());
            response.put("RspCode", "99");
            response.put("Message", "Unknown error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private PaymentResponse processVNPayParams(Map<String, String> params) {
        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        long amount;
        try {
            amount = Long.parseLong(params.get("vnp_Amount")) / 100; // VNPay amount is *100
        } catch (NumberFormatException e) {
            log.error("Invalid amount format in VNPay params: {}", params.get("vnp_Amount"));
            throw new IllegalArgumentException("Invalid amount format");
        }

        return paymentService.processVNPayResponse(orderId, responseCode, transactionNo, amount);
    }
}