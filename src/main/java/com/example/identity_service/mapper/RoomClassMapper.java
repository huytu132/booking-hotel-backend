package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.RoomClassRequest;
import com.example.identity_service.dto.response.BedTypeResponse;
import com.example.identity_service.dto.response.FeatureResponse;
import com.example.identity_service.dto.response.HotelResponse;
import com.example.identity_service.dto.response.RoomClassResponse;
import com.example.identity_service.entity.BedType;
import com.example.identity_service.entity.Feature;
import com.example.identity_service.entity.Hotel;
import com.example.identity_service.entity.RoomClass;
import com.example.identity_service.entity.RoomImage;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoomClassMapper {

    // RequestDTO -> Entity
    @Mapping(source = "hotelId", target = "hotel.id")
    @Mapping(target = "bedTypes", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "roomImages", ignore = true)
    RoomClass toEntity(RoomClassRequest requestDTO);

    // Entity -> ResponseDTO
    @Mapping(source = "hotel", target = "hotel", qualifiedByName = "mapHotelToResponse")
    @Mapping(source = "bedTypes", target = "bedTypes", qualifiedByName = "mapBedTypesToResponse")
    @Mapping(source = "features", target = "features", qualifiedByName = "mapFeaturesToResponse")
    @Mapping(source = "roomImages", target = "roomImagePaths", qualifiedByName = "mapRoomImagesToPaths")
    RoomClassResponse toResponseDTO(RoomClass roomClass);

    // Update entity từ request
    @Mapping(source = "hotelId", target = "hotel.id")
    @Mapping(target = "bedTypes", ignore = true)
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "roomImages", ignore = true)
    void updateEntityFromRequestDTO(RoomClassRequest requestDTO, @MappingTarget RoomClass roomClass);

    // ====== Helpers ======
    @Named("mapBedTypesToResponse")
    default List<BedTypeResponse> mapBedTypesToResponse(List<BedType> bedTypes) {
        return bedTypes != null
                ? bedTypes.stream()
                .map(b -> BedTypeResponse.builder()
                        .id(b.getId())
                        .bedName(b.getBedName()) // sửa lại đúng field
                        .build())
                .collect(Collectors.toList())
                : null;
    }

    @Named("mapFeaturesToResponse")
    default List<FeatureResponse> mapFeaturesToResponse(List<Feature> features) {
        return features != null
                ? features.stream()
                .map(f -> FeatureResponse.builder()
                        .id(f.getId())
                        .featureName(f.getFeatureName()) // sửa lại đúng field
                        .build())
                .collect(Collectors.toList())
                : null;
    }

    @Named("mapRoomImagesToPaths")
    default List<String> mapRoomImagesToPaths(List<RoomImage> roomImages) {
        return roomImages != null
                ? roomImages.stream().map(RoomImage::getPath).collect(Collectors.toList())
                : null;
    }

    @Named("mapHotelToResponse")
    default HotelResponse mapHotelToResponse(Hotel hotel) {
        if (hotel == null) return null;
        return HotelResponse.builder()
                .id(hotel.getId())
                .hotelName(hotel.getHotelName())
                .location(hotel.getLocation())
                .description(hotel.getDescription())
                .imageUrl(hotel.getImageUrl())
                .build();
    }
}
