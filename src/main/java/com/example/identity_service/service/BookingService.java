package com.example.identity_service.service;

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
import jakarta.servlet.http.HttpServletRequest;
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
    private final BookingRoomClassRepository bookingRoomClassRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomClassRepository roomClassRepository;
    private final AddonRepository addonRepository;
    private final BookingMapper bookingMapper;
    private final VNPayService vnPayService;

    // 1. Lấy giỏ hàng hiện tại của user
    public CartResponse getCart() {
        User currentUser = getCurrentUser();

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

        // Kiểm tra RoomClass có tồn tại không
        RoomClass roomClass = roomClassRepository.findById(request.getRoomClassId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_CLASS_NOT_FOUND));

        // Kiểm tra số lượng phòng khả dụng
        if (!isRoomClassAvailable(roomClass.getId(), request.getCheckinDate(), request.getCheckoutDate(), request.getQuantity())) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        // Kiểm tra số lượng khách hợp lệ
        if (request.getNumAdults() + (request.getNumChildren() != null ? request.getNumChildren() : 0) > roomClass.getCapacity() * request.getQuantity()) {
            throw new AppException(ErrorCode.GUEST_EXCEEDS_CAPACITY);
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

        // Kiểm tra xem RoomClass đã có trong cart với cùng ngày check-in/check-out chưa
        boolean roomClassExistsInCart = cartBooking.getBookingRoomClasses().stream()
                .anyMatch(br -> br.getRoomClass().getId().equals(request.getRoomClassId()) &&
                        br.getCheckinDate().equals(request.getCheckinDate()) &&
                        br.getCheckoutDate().equals(request.getCheckoutDate()));

        if (roomClassExistsInCart) {
            throw new AppException(ErrorCode.ROOM_CLASS_ALREADY_IN_CART);
        }

        // Tính giá phòng
        long nights = ChronoUnit.DAYS.between(request.getCheckinDate(), request.getCheckoutDate());
        BigDecimal roomPrice = roomClass.getPriceOriginal();
        Integer discountPercent = roomClass.getDiscountPercent();

        if (discountPercent != null && discountPercent > 0) {
            BigDecimal discount = roomPrice.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            roomPrice = roomPrice.subtract(discount);
        }

        BigDecimal roomSubtotal = roomPrice.multiply(BigDecimal.valueOf(request.getQuantity())).multiply(BigDecimal.valueOf(nights));

        // Tạo BookingRoomClass
        BookingRoomClass bookingRoomClass = BookingRoomClass.builder()
                .booking(cartBooking)
                .roomClass(roomClass)
                .quantity(request.getQuantity())
                .checkinDate(request.getCheckinDate())
                .checkoutDate(request.getCheckoutDate())
                .numAdults(request.getNumAdults())
                .numChildren(request.getNumChildren() != null ? request.getNumChildren() : 0)
                .roomPrice(roomPrice)
                .subtotal(roomSubtotal)
                .status("IN_CART")
                .rooms(new ArrayList<>()) // Ban đầu không có Room
                .build();

        // Thêm addons nếu có
        BigDecimal addonTotal = BigDecimal.ZERO;
        List<BookingRoomClassAddon> bookingAddons = new ArrayList<>();
        if (request.getAddons() != null && !request.getAddons().isEmpty()) {
            for (BookingRoomAddonRequest addonReq : request.getAddons()) {
                Addon addon = addonRepository.findById(addonReq.getAddonId())
                        .orElseThrow(() -> new AppException(ErrorCode.ADDON_NOT_FOUND));

                if (addonReq.getQuantity() < 0) {
                    throw new AppException(ErrorCode.INVALID_QUANTITY);
                }

                BigDecimal addonSubtotal = addon.getPrice().multiply(BigDecimal.valueOf(addonReq.getQuantity()));

                BookingRoomClassAddon bookingAddon = BookingRoomClassAddon.builder()
                        .bookingRoomClass(bookingRoomClass)
                        .addon(addon)
                        .quantity(addonReq.getQuantity())
                        .price(addon.getPrice())
                        .subtotal(addonSubtotal)
                        .build();

                bookingAddons.add(bookingAddon);
                addonTotal = addonTotal.add(addonSubtotal);
            }
        }

        bookingRoomClass.setBookingRoomClassAddons(bookingAddons);
        bookingRoomClass.setSubtotal(roomSubtotal.add(addonTotal));

        bookingRoomClassRepository.save(bookingRoomClass);

        // Cập nhật tổng của booking
        cartBooking.setTotalRoom(cartBooking.getTotalRoom() + request.getQuantity());
        cartBooking.setBookingAmount(cartBooking.getBookingAmount().add(bookingRoomClass.getSubtotal()));
        bookingRepository.save(cartBooking);

        return convertToCartResponse(cartBooking);
    }

    // 3. Xóa phòng khỏi giỏ hàng
    @Transactional
    public CartResponse removeFromCart(Integer bookingRoomClassId) {
        User currentUser = getCurrentUser();

        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        BookingRoomClass bookingRoomClass = bookingRoomClassRepository.findById(bookingRoomClassId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_ROOM_CLASS_NOT_FOUND));

        // Kiểm tra booking room class thuộc về cart của user
        if (!bookingRoomClass.getBooking().getId().equals(cartBooking.getId())) {
            throw new AppException(ErrorCode.BOOKING_ROOM_CLASS_NOT_IN_CART);
        }

        // Cập nhật tổng
        cartBooking.setTotalRoom(cartBooking.getTotalRoom() - bookingRoomClass.getQuantity());
        cartBooking.setBookingAmount(cartBooking.getBookingAmount().subtract(bookingRoomClass.getSubtotal()));

        // Xóa booking room class
        bookingRoomClassRepository.delete(bookingRoomClass);
        bookingRepository.save(cartBooking);

        return convertToCartResponse(cartBooking);
    }

    // 4. Cập nhật số lượng addon trong giỏ hàng
    @Transactional
    public CartResponse updateCartItemAddons(Integer bookingRoomClassId, List<BookingRoomAddonRequest> addons) {
        User currentUser = getCurrentUser();

        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        BookingRoomClass bookingRoomClass = bookingRoomClassRepository.findById(bookingRoomClassId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_ROOM_CLASS_NOT_FOUND));

        // Kiểm tra booking room class thuộc về cart của user
        if (!bookingRoomClass.getBooking().getId().equals(cartBooking.getId())) {
            throw new AppException(ErrorCode.BOOKING_ROOM_CLASS_NOT_IN_CART);
        }

        // Xóa addons cũ
        BigDecimal oldSubtotal = bookingRoomClass.getSubtotal();
        bookingRoomClass.getBookingRoomClassAddons().clear();

        // Tính lại subtotal của room (chỉ tính giá phòng)
        long nights = ChronoUnit.DAYS.between(bookingRoomClass.getCheckinDate(), bookingRoomClass.getCheckoutDate());
        BigDecimal roomSubtotal = bookingRoomClass.getRoomPrice()
                .multiply(BigDecimal.valueOf(bookingRoomClass.getQuantity()))
                .multiply(BigDecimal.valueOf(nights));

        // Thêm addons mới
        BigDecimal addonTotal = BigDecimal.ZERO;
        if (addons != null && !addons.isEmpty()) {
            for (BookingRoomAddonRequest addonReq : addons) {
                Addon addon = addonRepository.findById(addonReq.getAddonId())
                        .orElseThrow(() -> new AppException(ErrorCode.ADDON_NOT_FOUND));

                if (addonReq.getQuantity() < 0) {
                    throw new AppException(ErrorCode.INVALID_QUANTITY);
                }

                BigDecimal addonSubtotal = addon.getPrice().multiply(BigDecimal.valueOf(addonReq.getQuantity()));

                BookingRoomClassAddon bookingAddon = BookingRoomClassAddon.builder()
                        .bookingRoomClass(bookingRoomClass)
                        .addon(addon)
                        .quantity(addonReq.getQuantity())
                        .price(addon.getPrice())
                        .subtotal(addonSubtotal)
                        .build();

                bookingRoomClass.getBookingRoomClassAddons().add(bookingAddon);
                addonTotal = addonTotal.add(addonSubtotal);
            }
        }

        bookingRoomClass.setSubtotal(roomSubtotal.add(addonTotal));
        bookingRoomClassRepository.save(bookingRoomClass);

        // Cập nhật tổng booking
        cartBooking.setBookingAmount(cartBooking.getBookingAmount().subtract(oldSubtotal).add(bookingRoomClass.getSubtotal()));
        bookingRepository.save(cartBooking);

        return convertToCartResponse(cartBooking);
    }

    // 5. Xóa toàn bộ giỏ hàng
    @Transactional
    public void clearCart() {
        User currentUser = getCurrentUser();

        bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .ifPresent(cartBooking -> bookingRepository.delete(cartBooking));
    }

    // 6. Checkout - chuyển cart thành booking
    @Transactional
    public String checkout(HttpServletRequest request) {
        User currentUser = getCurrentUser();

        Booking cartBooking = bookingRepository.findByUserAndBookingStatus(currentUser, BookingStatus.CART)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (cartBooking.getBookingRoomClasses().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

//         Kiểm tra lại tất cả RoomClass có đủ phòng không
        for (BookingRoomClass bookingRoomClass : cartBooking.getBookingRoomClasses()) {
            if (!isRoomClassAvailable(
                    bookingRoomClass.getRoomClass().getId(),
                    bookingRoomClass.getCheckinDate(),
                    bookingRoomClass.getCheckoutDate(),
                    bookingRoomClass.getQuantity())) {
                throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
            }
        }

        // Chuyển status từ CART sang PENDING
        cartBooking.setBookingStatus(BookingStatus.PENDING);

        // Cập nhật status của các booking room class
        for (BookingRoomClass bookingRoomClass : cartBooking.getBookingRoomClasses()) {
            bookingRoomClass.setStatus("PENDING");
        }

        // Comment phần gọi PaymentService (dự định dùng VNPay)
        // paymentService.processPayment(cartBooking);

        Booking savedBooking = bookingRepository.save(cartBooking);

        String paymentUrl = vnPayService.createPaymentUrl(cartBooking, request);

        return paymentUrl; // FE redirect sang VNPay

//        return bookingMapper.toResponse(savedBooking);
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
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Kiểm tra booking thuộc về user
        if (!(booking.getUser().getId() == currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return bookingMapper.toResponse(booking);
    }

    // 9. Hủy booking
    @Transactional
    public BookingResponse cancelBooking(Integer bookingId) {
        User currentUser = getCurrentUser();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        // Kiểm tra booking thuộc về user
        if (!(booking.getUser().getId() == currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Chỉ cho phép hủy booking PENDING hoặc CONFIRMED
        if (booking.getBookingStatus() != BookingStatus.PENDING &&
                booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_BOOKING);
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);

        // Cập nhật status của các booking room class
        for (BookingRoomClass bookingRoomClass : booking.getBookingRoomClasses()) {
            bookingRoomClass.setStatus("CANCELLED");
        }

        // Comment phần gọi PaymentService (dự định dùng VNPay)
        // paymentService.processCancellationRefund(booking);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toResponse(savedBooking);
    }

    // Helper methods
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private boolean isRoomClassAvailable(Integer roomClassId, LocalDateTime checkinDate, LocalDateTime checkoutDate, int requestedQuantity) {
        RoomClass roomClass = roomClassRepository.findById(roomClassId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_CLASS_NOT_FOUND));

        List<Room> availableRooms = roomRepository.findByRoomClassAndRoomStatus(roomClass, RoomStatusType.AVAILABLE);
        List<BookingRoomClass> conflictingBookings = bookingRoomClassRepository.findConflictingBookings(
                roomClassId, checkinDate, checkoutDate);

        int bookedRooms = conflictingBookings.stream()
                .mapToInt(BookingRoomClass::getQuantity)
                .sum();
        int totalRooms = availableRooms.size();
        int remainingRooms = totalRooms - bookedRooms;

        return remainingRooms >= requestedQuantity;
    }

    private CartResponse convertToCartResponse(Booking cartBooking) {
        List<BookingRoomResponse> items = cartBooking.getBookingRoomClasses().stream()
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