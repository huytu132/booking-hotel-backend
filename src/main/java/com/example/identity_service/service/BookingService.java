package com.example.identity_service.service;

import com.example.identity_service.dto.request.BookingRequest;
import com.example.identity_service.dto.request.BookingRoomRequest;
import com.example.identity_service.dto.request.BookingRoomAddonRequest;
import com.example.identity_service.dto.response.BookingResponse;
import com.example.identity_service.dto.response.BookingRoomResponse;
import com.example.identity_service.dto.response.CartResponse;
import com.example.identity_service.entity.*;
import com.example.identity_service.enums.BookingStatus;
import com.example.identity_service.enums.RoomStatusType;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.BookingMapper;
import com.example.identity_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AddonRepository addonRepository;
    private final BookingMapper bookingMapper;

    // 1. Lấy giỏ hàng hiện tại của user
    public CartResponse getCart() {
        User currentUser = getCurrentUser();

        // Tìm booking với status CART
        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElse(null);

        if (cartBooking == null) {
            return CartResponse.builder()
                    .userId(currentUser.getId())
                    .items(new ArrayList<>())
                    .totalAmount(BigDecimal.ZERO)
                    .totalRooms(0)
                    .build();
        }

        return convertToCartResponse(cartBooking);
    }

    // 2. Thêm phòng vào giỏ hàng
    @Transactional
    public CartResponse addToCart(BookingRoomRequest request) {
        User currentUser = getCurrentUser();

        // Kiểm tra phòng có tồn tại và available không
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Kiểm tra phòng có available trong khoảng thời gian không
        if (!isRoomAvailable(room.getId(), request.getCheckinDate(), request.getCheckoutDate())) {
            throw new RuntimeException("Room is not available for selected dates");
        }

        // Lấy hoặc tạo cart booking
        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseGet(() -> {
                    Booking newCart = Booking.builder()
                            .user(currentUser)
                            .bookingStatus(BookingStatus.CART)
                            .totalRoom(0)
                            .bookingAmount(BigDecimal.ZERO)
                            .build();
                    return bookingRepository.save(newCart);
                });

        // Kiểm tra xem phòng đã có trong cart chưa
        boolean roomExistsInCart = cartBooking.getBookingRooms().stream()
                .anyMatch(br -> br.getRoom().getId() == room.getId() &&
                        br.getCheckinDate().equals(request.getCheckinDate()) &&
                        br.getCheckoutDate().equals(request.getCheckoutDate()));

        if (roomExistsInCart) {
            throw new RuntimeException("Room already exists in cart for these dates");
        }

        // Tính giá phòng
        long nights = ChronoUnit.DAYS.between(request.getCheckinDate(), request.getCheckoutDate());
        BigDecimal roomPrice = room.getRoomClass().getPriceOriginal();
        Integer discountPercent = room.getRoomClass().getDiscountPercent();

        if (discountPercent != null && discountPercent > 0) {
            BigDecimal discount = roomPrice.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            roomPrice = roomPrice.subtract(discount);
        }

        BigDecimal roomSubtotal = roomPrice.multiply(BigDecimal.valueOf(nights));

        // Tạo booking room
        BookingRoom bookingRoom = BookingRoom.builder()
                .booking(cartBooking)
                .room(room)
                .checkinDate(request.getCheckinDate())
                .checkoutDate(request.getCheckoutDate())
                .numAdults(request.getNumAdults())
                .numChildren(request.getNumChildren() != null ? request.getNumChildren() : 0)
                .roomPrice(roomPrice)
                .subtotal(roomSubtotal)
                .status("IN_CART")
                .build();

        // Thêm addons nếu có
        if (request.getAddons() != null && !request.getAddons().isEmpty()) {
            BigDecimal addonTotal = BigDecimal.ZERO;
            List<BookingRoomAddon> bookingAddons = new ArrayList<>();

            for (BookingRoomAddonRequest addonReq : request.getAddons()) {
                Addon addon = addonRepository.findById(addonReq.getAddonId())
                        .orElseThrow(() -> new RuntimeException("Addon not found"));

                BigDecimal addonSubtotal = addon.getPrice().multiply(BigDecimal.valueOf(addonReq.getQuantity()));

                BookingRoomAddon bookingAddon = BookingRoomAddon.builder()
                        .bookingRoom(bookingRoom)
                        .addon(addon)
                        .quantity(addonReq.getQuantity())
                        .price(addon.getPrice())
                        .subtotal(addonSubtotal)
                        .build();

                bookingAddons.add(bookingAddon);
                addonTotal = addonTotal.add(addonSubtotal);
            }

            bookingRoom.setBookingRoomAddons(bookingAddons);
            bookingRoom.setSubtotal(bookingRoom.getSubtotal().add(addonTotal));
        }

        bookingRoomRepository.save(bookingRoom);

        // Cập nhật tổng của booking
        cartBooking.setTotalRoom(cartBooking.getTotalRoom() + 1);
        cartBooking.setBookingAmount(cartBooking.getBookingAmount().add(bookingRoom.getSubtotal()));
        bookingRepository.save(cartBooking);

        return convertToCartResponse(cartBooking);
    }

    // 3. Xóa phòng khỏi giỏ hàng
    @Transactional
    public CartResponse removeFromCart(Integer bookingRoomId) {
        User currentUser = getCurrentUser();

        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        BookingRoom bookingRoom = bookingRoomRepository.findById(bookingRoomId)
                .orElseThrow(() -> new RuntimeException("Booking room not found"));

        // Kiểm tra booking room thuộc về cart của user
        if (bookingRoom.getBooking().getId() != cartBooking.getId()) {
            throw new RuntimeException("Booking room does not belong to your cart");
        }

        // Cập nhật tổng
        cartBooking.setTotalRoom(cartBooking.getTotalRoom() - 1);
        cartBooking.setBookingAmount(cartBooking.getBookingAmount().subtract(bookingRoom.getSubtotal()));

        // Xóa booking room
        bookingRoomRepository.delete(bookingRoom);
        bookingRepository.save(cartBooking);

        return convertToCartResponse(cartBooking);
    }

    // 4. Cập nhật số lượng addon trong giỏ hàng
    @Transactional
    public CartResponse updateCartItemAddons(Integer bookingRoomId, List<BookingRoomAddonRequest> addons) {
        User currentUser = getCurrentUser();

        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        BookingRoom bookingRoom = bookingRoomRepository.findById(bookingRoomId)
                .orElseThrow(() -> new RuntimeException("Booking room not found"));

        // Kiểm tra booking room thuộc về cart của user
        if (bookingRoom.getBooking().getId() != cartBooking.getId()) {
            throw new RuntimeException("Booking room does not belong to your cart");
        }

        // Xóa addons cũ
        bookingRoom.getBookingRoomAddons().clear();
        BigDecimal oldSubtotal = bookingRoom.getSubtotal();

        // Tính lại subtotal của room (chỉ tính giá phòng)
        long nights = ChronoUnit.DAYS.between(bookingRoom.getCheckinDate(), bookingRoom.getCheckoutDate());
        BigDecimal roomSubtotal = bookingRoom.getRoomPrice().multiply(BigDecimal.valueOf(nights));

        // Thêm addons mới
        BigDecimal addonTotal = BigDecimal.ZERO;
        if (addons != null && !addons.isEmpty()) {
            for (BookingRoomAddonRequest addonReq : addons) {
                Addon addon = addonRepository.findById(addonReq.getAddonId())
                        .orElseThrow(() -> new RuntimeException("Addon not found"));

                BigDecimal addonSubtotal = addon.getPrice().multiply(BigDecimal.valueOf(addonReq.getQuantity()));

                BookingRoomAddon bookingAddon = BookingRoomAddon.builder()
                        .bookingRoom(bookingRoom)
                        .addon(addon)
                        .quantity(addonReq.getQuantity())
                        .price(addon.getPrice())
                        .subtotal(addonSubtotal)
                        .build();

                bookingRoom.getBookingRoomAddons().add(bookingAddon);
                addonTotal = addonTotal.add(addonSubtotal);
            }
        }

        bookingRoom.setSubtotal(roomSubtotal.add(addonTotal));
        bookingRoomRepository.save(bookingRoom);

        // Cập nhật tổng booking
        cartBooking.setBookingAmount(cartBooking.getBookingAmount().subtract(oldSubtotal).add(bookingRoom.getSubtotal()));
        bookingRepository.save(cartBooking);

        return convertToCartResponse(cartBooking);
    }

    // 5. Xóa toàn bộ giỏ hàng
    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();

        bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .ifPresent(cartBooking -> {
                    bookingRepository.delete(cartBooking);
                });
    }

    // 6. Checkout - chuyển cart thành booking
    @Transactional
    public BookingResponse checkout() {
        User currentUser = getCurrentUser();

        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cartBooking.getBookingRooms().isEmpty()) {
            throw new RuntimeException("Cart has no items");
        }

        // Kiểm tra lại tất cả phòng có available không
        for (BookingRoom bookingRoom : cartBooking.getBookingRooms()) {
            if (!isRoomAvailable(bookingRoom.getRoom().getId(),
                    bookingRoom.getCheckinDate(),
                    bookingRoom.getCheckoutDate())) {
                throw new RuntimeException("Room " + bookingRoom.getRoom().getRoomNumber() +
                        " is no longer available");
            }
        }

        // Chuyển status từ CART sang PENDING
        cartBooking.setBookingStatus(BookingStatus.PENDING);

        // Cập nhật status của các booking room
        for (BookingRoom bookingRoom : cartBooking.getBookingRooms()) {
            bookingRoom.setStatus("PENDING");
        }

        Booking savedBooking = bookingRepository.save(cartBooking);

        return bookingMapper.toResponse(savedBooking);
    }

    // 7. Lấy danh sách bookings của user (không bao gồm cart)
    public List<BookingResponse> getMyBookings() {
        User currentUser = getCurrentUser();

        return bookingRepository.findByUserAndBookingStatusNot(currentUser, BookingStatus.CART)
                .stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 8. Lấy chi tiết booking
    public BookingResponse getBookingById(Integer bookingId) {
        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Kiểm tra booking thuộc về user
        if (booking.getUser().getId() != currentUser.getId()) {
            throw new RuntimeException("Booking does not belong to you");
        }

        return bookingMapper.toResponse(booking);
    }

    // 9. Hủy booking
    @Transactional
    public BookingResponse cancelBooking(Integer bookingId) {
        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Kiểm tra booking thuộc về user
        if (booking.getUser().getId() != currentUser.getId()) {
            throw new RuntimeException("Booking does not belong to you");
        }

        // Chỉ cho phép hủy booking PENDING hoặc CONFIRMED
        if (booking.getBookingStatus() != BookingStatus.PENDING &&
                booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Cannot cancel booking with status: " + booking.getBookingStatus());
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Cập nhật status của các booking room
        for (BookingRoom bookingRoom : booking.getBookingRooms()) {
            bookingRoom.setStatus("CANCELLED");
        }

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    // Helper methods
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private boolean isRoomAvailable(Integer roomId, LocalDateTime checkinDate, LocalDateTime checkoutDate) {
        List<BookingRoom> conflictingBookings = bookingRoomRepository.findConflictingBookings(
                roomId, checkinDate, checkoutDate);
        return conflictingBookings.isEmpty();
    }

    private CartResponse convertToCartResponse(Booking cartBooking) {
        List<BookingRoomResponse> items = cartBooking.getBookingRooms().stream()
                .map(bookingMapper::toBookingRoomResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .bookingId(cartBooking.getId())
                .userId(cartBooking.getUser().getId())
                .items(items)
                .totalAmount(cartBooking.getBookingAmount())
                .totalRooms(cartBooking.getTotalRoom())
                .build();
    }
}