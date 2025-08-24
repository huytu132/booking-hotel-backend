package com.example.identity_service.service;

import com.example.identity_service.dto.request.RoomRequest;
import com.example.identity_service.dto.response.RoomResponse;
import com.example.identity_service.entity.Room;
import com.example.identity_service.entity.RoomClass;
import com.example.identity_service.mapper.RoomMapper;
import com.example.identity_service.repository.BookingRoomClassRepository;
import com.example.identity_service.repository.RoomClassRepository;
import com.example.identity_service.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomClassRepository roomClassRepository;
    private final BookingRoomClassRepository bookingRoomClassRepository;
    private final RoomMapper roomMapper;

    @Transactional
    public RoomResponse createRoom(RoomRequest requestDTO) {
        // Kiểm tra trùng roomNumber
        if (roomRepository.existsByRoomNumber(requestDTO.getRoomNumber())) {
            throw new RuntimeException("Room number " + requestDTO.getRoomNumber() + " already exists");
        }

        // Kiểm tra roomClassId
        RoomClass roomClass = roomClassRepository.findById(requestDTO.getRoomClassId())
                .orElseThrow(() -> new RuntimeException("Room class not found with id: " + requestDTO.getRoomClassId()));

        Room room = roomMapper.toEntity(requestDTO);
        room.setRoomClass(roomClass);
        room = roomRepository.save(room);
        return roomMapper.toResponse(room);
    }

    public RoomResponse getRoomById(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return roomMapper.toResponse(room);
    }

    public List<RoomResponse> getRoomsByRoomClassId(Integer roomClassId) {
        return roomRepository.findByRoomClassId(roomClassId).stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse updateRoom(Integer id, RoomRequest requestDTO) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        // Kiểm tra trùng roomNumber
        if (!room.getRoomNumber().equals(requestDTO.getRoomNumber()) &&
                roomRepository.existsByRoomNumber(requestDTO.getRoomNumber())) {
            throw new RuntimeException("Room number " + requestDTO.getRoomNumber() + " already exists");
        }

        // Kiểm tra roomClassId
        RoomClass roomClass = roomClassRepository.findById(requestDTO.getRoomClassId())
                .orElseThrow(() -> new RuntimeException("Room class not found with id: " + requestDTO.getRoomClassId()));

        roomMapper.updateEntityFromRequest(requestDTO, room);
        room.setRoomClass(roomClass);
        roomRepository.save(room);
        return roomMapper.toResponse(room);
    }

    @Transactional
    public void deleteRoom(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        // Kiểm tra xem phòng có liên quan đến BookingRoom không
        if (!room.getBookingRoomClasses().isEmpty()) {
            throw new RuntimeException("Cannot delete Room with associated bookings");
        }

        roomRepository.deleteById(id);
    }

//    public List<RoomResponse> findAvailableRooms(Integer roomClassId, LocalDateTime checkinDate, LocalDateTime checkoutDate) {
//        if (checkinDate.isAfter(checkoutDate) || checkinDate.isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Invalid date range: check-in must be before check-out and not in the past");
//        }
//
//        List<Room> availableRooms = roomClassId != null
//                ? roomRepository.findAvailableRoomsByRoomClassId(roomClassId, checkinDate, checkoutDate)
//                : roomRepository.findAllAvailableRooms(checkinDate, checkoutDate);
//
//        return availableRooms.stream()
//                .map(roomMapper::toResponse)
//                .collect(Collectors.toList());
//    }
}