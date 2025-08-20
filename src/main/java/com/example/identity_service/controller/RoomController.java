package com.example.identity_service.controller;

import com.example.identity_service.dto.request.RoomRequest;
import com.example.identity_service.dto.response.RoomResponse;
import com.example.identity_service.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest requestDTO) {
        RoomResponse responseDTO = roomService.createRoom(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Integer id) {
        RoomResponse responseDTO = roomService.getRoomById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/room-class/{roomClassId}")
    public ResponseEntity<List<RoomResponse>> getRoomsByRoomClassId(@PathVariable Integer roomClassId) {
        List<RoomResponse> responseDTOs = roomService.getRoomsByRoomClassId(roomClassId);
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Integer id, @Valid @RequestBody RoomRequest requestDTO) {
        RoomResponse responseDTO = roomService.updateRoom(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> findAvailableRooms(
            @RequestParam(required = false) Integer roomClassId,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime checkinDate,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime checkoutDate) {
        List<RoomResponse> responseDTOs = roomService.findAvailableRooms(roomClassId, checkinDate, checkoutDate);
        return ResponseEntity.ok(responseDTOs);
    }
}