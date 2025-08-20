package com.example.identity_service.service;

import com.example.identity_service.dto.request.FeatureRequest;
import com.example.identity_service.dto.response.FeatureResponse;
import com.example.identity_service.entity.Feature;
import com.example.identity_service.mapper.FeatureMapper;
import com.example.identity_service.repository.FeatureRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeatureService {

    FeatureRepository featureRepository;
    FeatureMapper featureMapper;

    public FeatureResponse createFeature(FeatureRequest requestDTO) {
        if (featureRepository.existsByFeatureName(requestDTO.getFeatureName())) {
            throw new RuntimeException("Feature with name " + requestDTO.getFeatureName() + " already exists");
        }
        Feature feature = featureMapper.toEntity(requestDTO);
        feature = featureRepository.save(feature);
        return featureMapper.toResponse(feature);
    }

    public FeatureResponse getFeatureById(Integer id) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feature not found with id: " + id));
        return featureMapper.toResponse(feature);
    }

    public List<FeatureResponse> getAllFeatures() {
        return featureRepository.findAll().stream()
                .map(featureMapper::toResponse)
                .collect(Collectors.toList());
    }

    public FeatureResponse updateFeature(Integer id, FeatureRequest requestDTO) {
        Feature existingFeature = featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feature not found with id: " + id));

        if (!existingFeature.getFeatureName().equals(requestDTO.getFeatureName()) &&
                featureRepository.existsByFeatureName(requestDTO.getFeatureName())) {
            throw new RuntimeException("Feature with name " + requestDTO.getFeatureName() + " already exists");
        }

        featureMapper.updateEntityFromRequest(requestDTO, existingFeature);
        featureRepository.save(existingFeature);
        return featureMapper.toResponse(existingFeature);
    }

    public void deleteFeature(Integer id) {
        if (!featureRepository.existsById(id)) {
            throw new RuntimeException("Feature not found with id: " + id);
        }
        featureRepository.deleteById(id);
    }
}