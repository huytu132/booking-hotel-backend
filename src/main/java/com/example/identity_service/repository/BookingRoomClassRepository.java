package com.example.identity_service.repository;

import com.example.identity_service.entity.BookingRoomClass;
import com.example.identity_service.entity.RoomClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}