package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.AddonRequest;
import com.example.identity_service.dto.response.AddonResponse;
import com.example.identity_service.entity.Addon;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AddonMapper {

    AddonResponse toResponse(Addon addon);

    Addon toEntity(AddonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(AddonRequest request, @MappingTarget Addon addon);
}
