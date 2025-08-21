package com.example.identity_service.enums;

public enum BookingStatus {
    CART,        // Trạng thái giỏ hàng
    PENDING,     // Đã đặt, chờ xác nhận
    CONFIRMED,   // Đã xác nhận
    CHECKED_IN,  // Đã check-in
    CHECKED_OUT, // Đã check-out
    CANCELLED,   // Đã hủy
    NO_SHOW      // Không đến
}
