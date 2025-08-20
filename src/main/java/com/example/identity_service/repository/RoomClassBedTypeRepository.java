package com.example.identity_service.repository;

import com.example.identity_service.entity.RoomClassBedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomClassBedTypeRepository extends JpaRepository<RoomClassBedType, Integer> {
    void deleteByRoomClassId(Integer id);
}
