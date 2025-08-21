package com.example.identity_service.repository;

import com.example.identity_service.entity.Booking;
import com.example.identity_service.entity.User;
import com.example.identity_service.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer>, JpaSpecificationExecutor<Booking> {

    // === Basic queries ===
    Optional<Booking> findByUserAndBookingStatus(User user, BookingStatus status);

    List<Booking> findByUserAndBookingStatusNot(User user, BookingStatus status);

    List<Booking> findByUser(User user);

    List<Booking> findByBookingStatus(BookingStatus status);

    long countByBookingStatus(BookingStatus status);

    // === Complex queries ===
    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.bookingStatus IN :statuses")
    List<Booking> findByUserAndStatuses(@Param("user") User user,
                                        @Param("statuses") List<BookingStatus> statuses);

    // === Check-in/Check-out queries ===
    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status " +
            "AND EXISTS (SELECT br FROM BookingRoom br WHERE br.booking = b " +
            "AND br.checkinDate BETWEEN :startDate AND :endDate)")
    List<Booking> findUpcomingCheckIns(@Param("status") BookingStatus status,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status " +
            "AND EXISTS (SELECT br FROM BookingRoom br WHERE br.booking = b " +
            "AND br.checkinDate < :cutoffDate)")
    List<Booking> findOverdueCheckIns(@Param("status") BookingStatus status,
                                      @Param("cutoffDate") LocalDateTime cutoffDate);

    // === Statistics queries ===
    List<Booking> findByCreateAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createAt BETWEEN :startDate AND :endDate " +
            "AND b.bookingStatus != 'CART'")
    Integer countByCreateAtBetween(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status " +
            "AND b.createAt BETWEEN :startDate AND :endDate")
    Integer countByBookingStatusAndCreateAtBetween(@Param("status") BookingStatus status,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // === Today's statistics ===
    @Query("SELECT COUNT(b) FROM Booking b WHERE EXISTS " +
            "(SELECT br FROM BookingRoom br WHERE br.booking = b " +
            "AND br.checkinDate BETWEEN :startDate AND :endDate) " +
            "AND b.bookingStatus = 'CONFIRMED'")
    Integer countTodayCheckIns(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE EXISTS " +
            "(SELECT br FROM BookingRoom br WHERE br.booking = b " +
            "AND br.checkoutDate BETWEEN :startDate AND :endDate) " +
            "AND b.bookingStatus = 'CHECKED_IN'")
    Integer countTodayCheckOuts(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status " +
            "AND EXISTS (SELECT br FROM BookingRoom br WHERE br.booking = b " +
            "AND br.checkinDate BETWEEN :startDate AND :endDate)")
    Integer countUpcomingCheckIns(@Param("status") BookingStatus status,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status " +
            "AND EXISTS (SELECT br FROM BookingRoom br WHERE br.booking = b " +
            "AND br.checkinDate < :cutoffDate)")
    Integer countOverdueCheckIns(@Param("status") BookingStatus status,
                                 @Param("cutoffDate") LocalDateTime cutoffDate);

    // === Revenue report ===
    @Query(value = "SELECT h.id, h.hotel_name, COUNT(DISTINCT b.id) as total_bookings, " +
            "COUNT(br.id) as total_rooms, SUM(b.booking_amount) as total_revenue " +
            "FROM hotel h " +
            "LEFT JOIN room_class rc ON rc.hotel_id = h.id " +
            "LEFT JOIN room r ON r.room_class_id = rc.id " +
            "LEFT JOIN booking_room br ON br.room_id = r.id " +
            "LEFT JOIN booking b ON b.id = br.booking_id " +
            "WHERE b.create_at BETWEEN :startDate AND :endDate " +
            "AND b.booking_status IN ('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT') " +
            "GROUP BY h.id, h.hotel_name", nativeQuery = true)
    List<Object[]> getRevenueByHotel(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);
}