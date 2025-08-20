package com.example.identity_service.repository;

import com.example.identity_service.entity.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Integer> {
    boolean existsByRoomRoomClassId(Integer roomClassId);
}