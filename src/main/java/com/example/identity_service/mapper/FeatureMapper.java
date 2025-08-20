package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.FeatureRequest;
import com.example.identity_service.dto.response.FeatureResponse;
import com.example.identity_service.entity.Feature;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    // Ánh xạ từ FeatureRequestDTO sang Feature entity
    Feature toEntity(FeatureRequest requestDTO);

    // Ánh xạ từ Feature entity sang FeatureResponseDTO
    FeatureResponse toResponse(Feature feature);

    // Cập nhật Feature entity từ FeatureRequestDTO
    void updateEntityFromRequest(FeatureRequest requestDTO, @MappingTarget Feature feature);
}