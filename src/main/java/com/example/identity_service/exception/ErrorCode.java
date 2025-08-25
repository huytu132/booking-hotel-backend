package com.example.identity_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1007, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    ROOM_CLASS_NOT_FOUND(1008, "Room class not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_AVAILABLE(1009, "Room is not available", HttpStatus.BAD_REQUEST),
    GUEST_EXCEEDS_CAPACITY(1010, "Number of guests exceeds room capacity", HttpStatus.BAD_REQUEST),
    ROOM_CLASS_ALREADY_IN_CART(1011, "Room class already exists in cart", HttpStatus.BAD_REQUEST),
    CART_NOT_FOUND(1012, "Cart not found", HttpStatus.NOT_FOUND),
    BOOKING_ROOM_CLASS_NOT_FOUND(1013, "Booking room class not found", HttpStatus.NOT_FOUND),
    BOOKING_ROOM_CLASS_NOT_IN_CART(1014, "Booking room class does not belong to cart", HttpStatus.BAD_REQUEST),
    ADDON_NOT_FOUND(1015, "Addon not found", HttpStatus.NOT_FOUND),
    INVALID_QUANTITY(1016, "Invalid quantity", HttpStatus.BAD_REQUEST),
    CART_EMPTY(1017, "Cart is empty", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1018, "Booking not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(1019, "Unauthorized access", HttpStatus.UNAUTHORIZED),
    CANNOT_CANCEL_BOOKING(1020, "Cannot cancel booking", HttpStatus.BAD_REQUEST),
    INVALID_STATE(1021, "Trạng thái không hợp lệ", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatus httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatus httpStatusCode;
}