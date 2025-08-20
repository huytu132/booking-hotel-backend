package com.example.identity_service.repository;

import com.example.identity_service.entity.BedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BedTypeRepository extends JpaRepository<BedType, Integer> {
    boolean existsByBedName(String bedName);
}
