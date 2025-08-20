package com.example.identity_service.repository;

import com.example.identity_service.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Integer> {
    void deleteByRoomClassId(Integer id);
}
