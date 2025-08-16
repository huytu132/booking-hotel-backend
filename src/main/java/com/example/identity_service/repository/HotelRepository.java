package com.example.identity_service.repository;

import com.example.identity_service.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    // Lấy các location duy nhất
    @Query(value = "SELECT DISTINCT h.location FROM hotel h", nativeQuery = true)
    List<String> findDistinctLocations();

    // Tìm kiếm theo tên và location
    @Query(value = "SELECT * FROM hotel h " +
            "WHERE (:name IS NULL OR LOWER(h.hotel_name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:location IS NULL OR LOWER(h.location) LIKE LOWER(CONCAT('%', :location, '%')))",
            nativeQuery = true)
    List<Hotel> searchHotels(@Param("name") String name, @Param("location") String location);
}
