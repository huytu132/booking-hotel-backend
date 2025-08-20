package com.example.identity_service.repository;

import com.example.identity_service.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Integer> {
    boolean existsByFeatureName(String featureName);
}