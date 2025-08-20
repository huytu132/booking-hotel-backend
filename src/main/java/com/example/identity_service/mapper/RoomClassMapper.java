package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.RoomClassRequest;
import com.example.identity_service.dto.response.RoomClassResponse;
import com.example.identity_service.entity.BedType;
import com.example.identity_service.entity.Feature;
import com.example.identity_service.entity.RoomClass;
import com.example.identity_service.entity.RoomImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomClassMapper {

    // Ánh xạ từ RoomClassRequestDTO sang RoomClass entity
    @Mapping(source = "hotelId", target = "hotel.id")
    @Mapping(target = "bedTypes", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "roomImages", ignore = true)
    RoomClass toEntity(RoomClassRequest requestDTO);

    // Ánh xạ từ RoomClass entity sang RoomClassResponseDTO
    @Mapping(source = "hotel.id", target = "hotelId")
    @Mapping(source = "bedTypes", target = "bedTypeIds", qualifiedByName = "mapBedTypesToIds")
    @Mapping(source = "features", target = "featureIds", qualifiedByName = "mapFeaturesToIds")
    @Mapping(source = "roomImages", target = "roomImagePaths", qualifiedByName = "mapRoomImagesToPaths")
    RoomClassResponse toResponseDTO(RoomClass roomClass);

    // Cập nhật RoomClass entity từ RoomClassRequestDTO
    @Mapping(source = "hotelId", target = "hotel.id")
    @Mapping(target = "bedTypes", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "roomImages", ignore = true)
    void updateEntityFromRequestDTO(RoomClassRequest requestDTO, @MappingTarget RoomClass roomClass);

    @Named("mapBedTypesToIds")
    default List<Integer> mapBedTypesToIds(List<BedType> bedTypes) {
        return bedTypes.stream().map(BedType::getId).collect(Collectors.toList());
    }

    @Named("mapFeaturesToIds")
    default List<Integer> mapFeaturesToIds(List<Feature> features) {
        return features.stream().map(Feature::getId).collect(Collectors.toList());
    }

    @Named("mapRoomImagesToPaths")
    default List<String> mapRoomImagesToPaths(List<RoomImage> roomImages) {
        return roomImages.stream().map(RoomImage::getPath).collect(Collectors.toList());
    }
}