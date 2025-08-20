package com.example.identity_service.repository;

import com.example.identity_service.entity.RoomClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomClassRepository extends JpaRepository<RoomClass, Integer> {
    boolean existsByRoomClassNameAndHotelId(String roomClassName, Integer hotelId);
    List<RoomClass> findByHotelId(Integer hotelId);
}