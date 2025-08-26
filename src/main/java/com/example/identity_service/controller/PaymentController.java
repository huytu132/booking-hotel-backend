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

    @GetMapping("/{id}")
    public PaymentResponse getPaymentById(@PathVariable Integer id) {
        return paymentService.getPaymentById(id);
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<String> paymentReturn(@RequestParam Map<String, String> params) {
        if (!vnPayService.validateResponse(params)) {
            return ResponseEntity.badRequest().body("Invalid VNPay response");
        }

        PaymentResponse paymentResponse = processVNPayParams(params);

        // redirect link về fe, fe get lấy paymentId rồi call api lấy status của payment để hiển th paid/failed
        String redirectUrl = "localhost:8080/api/payment?paymentId=" + paymentResponse.getId();
        return ResponseEntity.ok(redirectUrl);
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