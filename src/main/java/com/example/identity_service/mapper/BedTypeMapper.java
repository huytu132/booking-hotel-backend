package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.BedTypeRequest;
import com.example.identity_service.dto.response.BedTypeResponse;
import com.example.identity_service.entity.BedType;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BedTypeMapper {

    // Ánh xạ từ BedTypeRequestDTO sang BedType entity
    BedType toEntity(BedTypeRequest request);

    // Ánh xạ từ BedType entity sang BedTypeResponseDTO
    BedTypeResponse toResponse(BedType bedType);

    // Cập nhật BedType entity từ BedTypeRequestDTO
    void updateEntityFromRequest(BedTypeRequest request, @MappingTarget BedType bedType);
}