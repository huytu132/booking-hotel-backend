package com.example.identity_service.controller;

import com.example.identity_service.dto.response.PaymentResponse;
import com.example.identity_service.service.PaymentService;
import com.example.identity_service.service.VNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final VNPayService vnPayService;
    private final PaymentService paymentService;

    @GetMapping("/vnpay")
    public ResponseEntity<String> createPayment(@RequestParam("amount") long amount,
                                                @RequestParam("orderId") String orderId,
                                                HttpServletRequest request) {
        try {
            String url = vnPayService.createPaymentUrl(amount, orderId, request);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            log.error("Error creating payment URL for orderId: {}", orderId, e);
            return ResponseEntity.badRequest().body("Error creating payment URL: " + e.getMessage());
        }
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> paymentReturn(@RequestParam Map<String, String> params) {
        log.info("VNPay return params: {}", params);

        if (!vnPayService.validateResponse(params)) {
            log.warn("Invalid VNPay response checksum");
            return ResponseEntity.badRequest().body("Invalid VNPay response (checksum failed)");
        }

        try {
            PaymentResponse paymentResponse = processVNPayParams(params, true);
            log.info(paymentResponse.toString());
            String redirectUrl = paymentResponse.getPaymentStatus().equals("PAID")
                    ? "http://your-frontend.com/payment/success?paymentId=" + paymentResponse.getId()
                    : "http://your-frontend.com/payment/failure?paymentId=" + paymentResponse.getId();
            return ResponseEntity.ok(redirectUrl);
        } catch (IllegalArgumentException e) {
            log.error("Error processing VNPay return: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error processing VNPay response: " + e.getMessage());
        }
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
            processVNPayParams(params, false);
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

    private PaymentResponse processVNPayParams(Map<String, String> params, boolean isReturn) {
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

        return paymentService.processVNPayResponse(orderId, responseCode, transactionNo, amount, isReturn);
    }
}