package com.example.identity_service.repository;

import com.example.identity_service.entity.Room;
import com.example.identity_service.entity.RoomClass;
import com.example.identity_service.enums.RoomStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    boolean existsByRoomNumber(String roomNumber);
    List<Room> findByRoomClassId(Integer roomClassId);

    Long countByRoomStatus(RoomStatusType status);

//    @Query("SELECT r FROM Room r WHERE r.roomClass.id = :roomClassId " +
//            "AND NOT EXISTS (SELECT br FROM BookingRoom br WHERE br.room.id = r.id " +
//            "AND br.checkinDate < :checkoutDate " +
//            "AND br.checkoutDate > :checkinDate)")
//    List<Room> findAvailableRoomsByRoomClassId(Integer roomClassId, LocalDateTime checkinDate, LocalDateTime checkoutDate);
//
//    @Query("SELECT r FROM Room r WHERE NOT EXISTS (SELECT br FROM BookingRoom br WHERE br.room.id = r.id " +
//            "AND br.checkinDate < :checkoutDate " +
//            "AND br.checkoutDate > :checkinDate)")
//    List<Room> findAllAvailableRooms(LocalDateTime checkinDate, LocalDateTime checkoutDate);

    List<Room> findByRoomClassAndRoomStatus(RoomClass roomClass, RoomStatusType roomStatusType);

}
