package com.example.identity_service.controller;

import com.example.identity_service.dto.request.BookingRoomRequest;
import com.example.identity_service.dto.request.BookingRoomAddonRequest;
import com.example.identity_service.dto.response.BookingResponse;
import com.example.identity_service.dto.response.CartResponse;
import com.example.identity_service.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // === CART OPERATIONS ===

    // Lấy giỏ hàng hiện tại
    @GetMapping("/cart")
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(bookingService.getCart());
    }

    // Thêm phòng vào giỏ hàng
    @PostMapping("/cart/add")
    public ResponseEntity<CartResponse> addToCart(@RequestBody BookingRoomRequest request) {
        return ResponseEntity.ok(bookingService.addToCart(request));
    }

    // Xóa phòng khỏi giỏ hàng
    @DeleteMapping("/cart/remove/{bookingRoomId}")
    public ResponseEntity<CartResponse> removeFromCart(@PathVariable Integer bookingRoomId) {
        return ResponseEntity.ok(bookingService.removeFromCart(bookingRoomId));
    }

    // Cập nhật addon của phòng trong giỏ hàng
    @PutMapping("/cart/update-addons/{bookingRoomId}")
    public ResponseEntity<CartResponse> updateCartItemAddons(
            @PathVariable Integer bookingRoomId,
            @RequestBody List<BookingRoomAddonRequest> addons) {
        return ResponseEntity.ok(bookingService.updateCartItemAddons(bookingRoomId, addons));
    }

    // Xóa toàn bộ giỏ hàng
    @DeleteMapping("/cart/clear")
    public ResponseEntity<Void> clearCart() {
        bookingService.clearCart();
        return ResponseEntity.noContent().build();
    }

    // Checkout - chuyển cart thành booking
    @PostMapping("/cart/checkout")
    public ResponseEntity<String> checkout(HttpServletRequest request) {
        String paymentUrl = bookingService.checkout(request);

        // Trả về URL cho FE redirect
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentUrl);
    }

    // === BOOKING OPERATIONS ===

    // Lấy danh sách booking của user (không bao gồm cart)
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    // Lấy chi tiết một booking
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    // Hủy booking
    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }
}