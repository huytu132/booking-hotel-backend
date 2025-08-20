package com.example.identity_service.mapper;

import com.example.identity_service.dto.request.RoomRequest;
import com.example.identity_service.dto.response.RoomResponse;
import com.example.identity_service.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    // Ánh xạ từ RoomRequestDTO sang Room entity
    @Mapping(source = "roomClassId", target = "roomClass.id")
    Room toEntity(RoomRequest requestDTO);

    // Ánh xạ từ Room entity sang RoomResponseDTO
    @Mapping(source = "roomClass.id", target = "roomClassId")
    RoomResponse toResponse(Room room);

    // Cập nhật Room entity từ RoomRequestDTO
    @Mapping(source = "roomClassId", target = "roomClass.id")
    void updateEntityFromRequest(RoomRequest requestDTO, @MappingTarget Room room);
}