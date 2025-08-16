package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.HotelRequest;
import com.example.identity_service.dto.response.HotelResponse;
import com.example.identity_service.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    HotelMapper INSTANCE = Mappers.getMapper(HotelMapper.class);

    @Mapping(target = "imageUrl", ignore = true) // xử lý image riêng trong service
    Hotel toEntity(HotelRequest dto);

    HotelResponse toDto(Hotel hotel);
}
