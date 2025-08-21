package com.example.identity_service.repository;

import com.example.identity_service.entity.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Integer> {

    // Method đã có từ trước
    boolean existsByRoomRoomClassId(Integer roomClassId);

    // THÊM METHOD NÀY để fix lỗi findConflictingBookings
    @Query("SELECT br FROM BookingRoom br " +
            "WHERE br.room.id = :roomId " +
            "AND br.booking.bookingStatus NOT IN ('CART', 'CANCELLED') " +
            "AND ((br.checkinDate < :checkoutDate AND br.checkoutDate > :checkinDate))")
    List<BookingRoom> findConflictingBookings(
            @Param("roomId") Integer roomId,
            @Param("checkinDate") LocalDateTime checkinDate,
            @Param("checkoutDate") LocalDateTime checkoutDate
    );

    // Thêm các method hữu ích khác
    List<BookingRoom> findByBookingId(Integer bookingId);

    @Query("SELECT br FROM BookingRoom br WHERE br.room.id = :roomId")
    List<BookingRoom> findByRoomId(@Param("roomId") Integer roomId);

    @Query("SELECT br FROM BookingRoom br WHERE br.booking.user.id = :userId")
    List<BookingRoom> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT br FROM BookingRoom br " +
            "WHERE br.checkinDate BETWEEN :startDate AND :endDate " +
            "AND br.booking.bookingStatus = 'CONFIRMED'")
    List<BookingRoom> findUpcomingCheckIns(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}