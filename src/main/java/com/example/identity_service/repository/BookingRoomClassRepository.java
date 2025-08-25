package com.example.identity_service.repository;

import com.example.identity_service.entity.BookingRoomClass;
import com.example.identity_service.entity.RoomClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRoomClassRepository extends JpaRepository<BookingRoomClass, Integer> {

    @Query("SELECT br FROM BookingRoomClass br " +
            "WHERE br.roomClass.id = :roomClassId " +
            "AND br.status NOT IN ('CANCELLED') " +
            "AND ((br.checkinDate < :checkoutDate AND br.checkoutDate > :checkinDate))")
    List<BookingRoomClass> findConflictingBookings(Integer roomClassId, LocalDateTime checkinDate, LocalDateTime checkoutDate);

    @Query("""
        SELECT COALESCE(SUM(brc.quantity), 0)
        FROM BookingRoomClass brc
        WHERE brc.roomClass.id = :roomClassId
          AND brc.status IN ('PENDING','CONFIRMED')
          AND brc.checkinDate < :checkoutDate
          AND brc.checkoutDate > :checkinDate
    """)
    int sumBookedQuantityOverlap(@Param("roomClassId") Integer roomClassId,
                                 @Param("checkinDate") LocalDateTime checkinDate,
                                 @Param("checkoutDate") LocalDateTime checkoutDate);
}