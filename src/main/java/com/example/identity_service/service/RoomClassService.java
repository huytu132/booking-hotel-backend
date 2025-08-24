package com.example.identity_service.service;

import com.example.identity_service.dto.request.RoomClassRequest;
import com.example.identity_service.dto.response.RoomClassResponse;
import com.example.identity_service.entity.*;
import com.example.identity_service.enums.RoomStatusType;
import com.example.identity_service.mapper.RoomClassMapper;
import com.example.identity_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomClassService {

    private final RoomClassRepository roomClassRepository;
    private final HotelRepository hotelRepository;
    private final BedTypeRepository bedTypeRepository;
    private final FeatureRepository featureRepository;
    private final RoomClassBedTypeRepository roomClassBedTypeRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomClassMapper roomClassMapper;
    private final RoomRepository roomRepository;

    @Transactional
    public RoomClassResponse createRoomClass(RoomClassRequest requestDTO) {
        // Kiểm tra trùng lặp roomClassName trong hotel
        if (roomClassRepository.existsByRoomClassNameAndHotelId(requestDTO.getRoomClassName(), requestDTO.getHotelId())) {
            throw new RuntimeException("Room class with name " + requestDTO.getRoomClassName() + " already exists in hotel");
        }

        // Kiểm tra hotelId
        Hotel hotel = hotelRepository.findById(requestDTO.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + requestDTO.getHotelId()));

        // Kiểm tra bedTypeIds
        List<BedType> bedTypes = requestDTO.getBedTypeIds().stream()
                .map(id -> bedTypeRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Bed type not found with id: " + id)))
                .collect(Collectors.toList());

        // Kiểm tra featureIds
        List<Feature> features = requestDTO.getFeatureIds().stream()
                .map(id -> featureRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Feature not found with id: " + id)))
                .collect(Collectors.toList());

        // Tạo RoomClass
        RoomClass roomClass = roomClassMapper.toEntity(requestDTO);
        roomClass.setHotel(hotel);
        roomClass.setBedTypes(bedTypes);
        roomClass.setFeatures(features);

        // Lưu RoomClass
        roomClass = roomClassRepository.save(roomClass);

        int quantity = Integer.parseInt(requestDTO.getQuantity());
        List<Room> rooms = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            Room room = Room.builder()
                    .roomClass(roomClass)
                    .roomStatus(RoomStatusType.AVAILABLE)
                    .roomNumber(roomClass.getRoomClassName() + "-" + i)
                    .build();
            rooms.add(room);
        }
        roomRepository.saveAll(rooms);
        roomClass.setRooms(rooms);

        // Lưu RoomImages
        if (requestDTO.getRoomImagePaths() != null) {
            RoomClass finalRoomClass = roomClass;
            List<RoomImage> roomImages = requestDTO.getRoomImagePaths().stream()
                    .map(path -> RoomImage.builder().roomClass(finalRoomClass).path(path).build())
                    .collect(Collectors.toList());
            roomImageRepository.saveAll(roomImages);
            roomClass.setRoomImages(roomImages);
        }

        return roomClassMapper.toResponseDTO(roomClass);
    }

    public RoomClassResponse getRoomClassById(Integer id) {
        RoomClass roomClass = roomClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room class not found with id: " + id));
        return roomClassMapper.toResponseDTO(roomClass);
    }

    public List<RoomClassResponse> getAllRoomClassesByHotel(Integer hotelId) {
        return roomClassRepository.findByHotelId(hotelId).stream()
                .map(roomClassMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomClassResponse updateRoomClass(Integer id, RoomClassRequest requestDTO) {
        RoomClass roomClass = roomClassRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room class not found with id: " + id));

        // Kiểm tra trùng lặp roomClassName trong hotel
        if (!roomClass.getRoomClassName().equals(requestDTO.getRoomClassName()) &&
                roomClassRepository.existsByRoomClassNameAndHotelId(requestDTO.getRoomClassName(), requestDTO.getHotelId())) {
            throw new RuntimeException("Room class with name " + requestDTO.getRoomClassName() + " already exists in hotel");
        }

        // Kiểm tra hotelId
        Hotel hotel = hotelRepository.findById(requestDTO.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with id: " + requestDTO.getHotelId()));

        // Kiểm tra bedTypeIds
        List<BedType> bedTypes = requestDTO.getBedTypeIds().stream()
                .map(bedTypeId -> bedTypeRepository.findById(bedTypeId)
                        .orElseThrow(() -> new RuntimeException("Bed type not found with id: " + bedTypeId)))
                .collect(Collectors.toList());

        // Kiểm tra featureIds
        List<Feature> features = requestDTO.getFeatureIds().stream()
                .map(featureId -> featureRepository.findById(featureId)
                        .orElseThrow(() -> new RuntimeException("Feature not found with id: " + featureId)))
                .collect(Collectors.toList());

        // Cập nhật RoomClass
        roomClassMapper.updateEntityFromRequestDTO(requestDTO, roomClass);
        roomClass.setHotel(hotel);
        roomClass.setBedTypes(bedTypes);
        roomClass.setFeatures(features);

        int newQuantity = Integer.parseInt(requestDTO.getQuantity());
        List<Room> existingRooms = roomRepository.findByRoomClassId(id);
        if (existingRooms.size() > newQuantity) {
            // Xóa bớt Room nếu quantity giảm
            List<Room> roomsToDelete = existingRooms.subList(newQuantity, existingRooms.size());
            for (Room room : roomsToDelete) {
                if (!room.getBookingRoomClasses().isEmpty()) {
                    throw new RuntimeException("Cannot delete Room with associated bookings");
                }
            }
            roomRepository.deleteAll(roomsToDelete);
        } else if (existingRooms.size() < newQuantity) {
            // Thêm Room nếu quantity tăng
            List<Room> newRooms = new ArrayList<>();
            for (int i = existingRooms.size() + 1; i <= newQuantity; i++) {
                Room room = Room.builder()
                        .roomClass(roomClass)
                        .roomStatus(RoomStatusType.AVAILABLE)
                        .roomNumber(roomClass.getRoomClassName() + "-" + i)
                        .build();
                newRooms.add(room);
            }
            roomRepository.saveAll(newRooms);
        }

        // Cập nhật RoomImages
        if (requestDTO.getRoomImagePaths() != null) {
            roomImageRepository.deleteByRoomClassId(id); // Xóa các hình ảnh cũ
            List<RoomImage> roomImages = requestDTO.getRoomImagePaths().stream()
                    .map(path -> RoomImage.builder().roomClass(roomClass).path(path).build())
                    .collect(Collectors.toList());
            roomImageRepository.saveAll(roomImages);
            roomClass.setRoomImages(roomImages);
        }

        roomClassRepository.save(roomClass);
        return roomClassMapper.toResponseDTO(roomClass);
    }

    @Transactional
    public void deleteRoomClass(Integer id) {
        if (!roomClassRepository.existsById(id)) {
            throw new RuntimeException("Room class not found with id: " + id);
        }

        // Kiểm tra xem có Room liên quan không
        List<Room> rooms = roomRepository.findByRoomClassId(id);
        for (Room room : rooms) {
            if (!room.getBookingRoomClasses().isEmpty()) {
                throw new RuntimeException("Cannot delete RoomClass with associated bookings");
            }
        }

        roomRepository.deleteAll(rooms);
        // Xóa các RoomImage liên quan
        roomImageRepository.deleteByRoomClassId(id);
        // Xóa các RoomClassBedType liên quan
        roomClassBedTypeRepository.deleteByRoomClassId(id);
        roomClassRepository.deleteById(id);
    }
}