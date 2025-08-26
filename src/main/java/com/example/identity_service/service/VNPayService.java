package com.example.identity_service.service;

import com.example.identity_service.configuration.VNPayConfig;
import com.example.identity_service.entity.Booking;
import com.example.identity_service.utils.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VNPayService {

    private final VNPayConfig vnPayConfig;

//    public String createPaymentUrl(long amount, String orderId, HttpServletRequest request) {
//        Map<String, String> params = vnPayConfig.getConfig();
//
//        // Thời gian tạo request
//        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//
//        String vnp_CreateDate = formatter.format(now.getTime());
//        params.put("vnp_CreateDate", vnp_CreateDate);
//
//        // Thời gian hết hạn (15 phút sau)
//        now.add(Calendar.MINUTE, 15);
//        String vnp_ExpireDate = formatter.format(now.getTime());
//        params.put("vnp_ExpireDate", vnp_ExpireDate);
//
//        // Số tiền (nhân 100 theo chuẩn VNPAY)
//        params.put("vnp_Amount", String.valueOf(amount * 100L));
//
//        // Mã tham chiếu đơn hàng
//        String ref = orderId + "-" + System.currentTimeMillis();
//        params.put("vnp_TxnRef", ref);
//
//        // Thông tin đơn hàng
//        params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
//
//        // IP
//        String ipAddr = VNPayUtil.getIpAddress(request);
//        params.put("vnp_IpAddr", ipAddr);
//
//        // Build query + hash
//        String queryString = VNPayUtil.createPaymentUrl(params, true);
//        String hashData = VNPayUtil.createPaymentUrl(params, false);
//        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
//
//        queryString += "&vnp_SecureHash=" + vnpSecureHash;
//
//        return vnPayConfig.getVnp_PayUrl() + "?" + queryString;
//    }

    public String createPaymentUrl(Booking booking, HttpServletRequest request) {
        Map<String, String> params = vnPayConfig.getConfig();

        // Thời gian tạo request
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String vnp_CreateDate = formatter.format(now.getTime());
        params.put("vnp_CreateDate", vnp_CreateDate);

        // Thời gian hết hạn (15 phút sau)
        now.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(now.getTime());
        params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Số tiền (nhân 100 theo chuẩn VNPAY)
        long amount = booking.getBookingAmount()
                .multiply(BigDecimal.valueOf(100L))
                .longValue();
        params.put("vnp_Amount", String.valueOf(amount));

        // Mã tham chiếu đơn hàng (bookingId là duy nhất)
        String ref = booking.getId() + "-" + System.currentTimeMillis();
        params.put("vnp_TxnRef", ref);

        // Thông tin đơn hàng
        params.put("vnp_OrderInfo", "Thanh toan don hang: " + booking.getId());

        // IP
        String ipAddr = VNPayUtil.getIpAddress(request);
        params.put("vnp_IpAddr", ipAddr);

        // Build query + hash
        String queryString = VNPayUtil.createPaymentUrl(params, true);
        String hashData = VNPayUtil.createPaymentUrl(params, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        queryString += "&vnp_SecureHash=" + vnpSecureHash;

        return vnPayConfig.getVnp_PayUrl() + "?" + queryString;
    }

    public boolean validateResponse(Map<String, String> params) {
        String vnp_SecureHash = params.remove("vnp_SecureHash"); // Remove hash for validation
        if (vnp_SecureHash == null) {
            return false;
        }

        String hashData = VNPayUtil.createPaymentUrl(params, false);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        return vnp_SecureHash.equals(calculatedHash);
    }
}