package com.example.identity_service.repository;

import com.example.identity_service.entity.Booking;
import com.example.identity_service.entity.Payment;
import com.example.identity_service.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByBooking(Booking booking);
    List<Payment> findByBookingOrderByPaymentDateDesc(Booking booking);
    Optional<Payment> findByBookingAndTransId(Booking booking, String transId);

    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.paymentStatus = 'PAID'")
    BigDecimal getTodayRevenue(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.paymentStatus = 'PAID'")
    BigDecimal getMonthlyRevenue(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.paymentDate BETWEEN :startDate AND :endDate " +
            "AND p.paymentStatus = 'PAID'")
    BigDecimal getRevenueByDate(@Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);

    List<Payment> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId " +
            "AND p.paymentStatus = :status")
    List<Payment> findByBookingIdAndStatus(@Param("bookingId") Integer bookingId,
                                           @Param("status") PaymentStatus status);
}