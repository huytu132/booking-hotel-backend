package com.example.identity_service.controller;

import com.example.identity_service.dto.request.RoomClassRequest;
import com.example.identity_service.dto.response.RoomClassResponse;
import com.example.identity_service.service.RoomClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-classes")
@RequiredArgsConstructor
public class RoomClassController {

    private final RoomClassService roomClassService;

    @PostMapping
    public ResponseEntity<RoomClassResponse> createRoomClass(@RequestBody RoomClassRequest requestDTO) {
        RoomClassResponse responseDTO = roomClassService.createRoomClass(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomClassResponse> getRoomClassById(@PathVariable Integer id) {
        RoomClassResponse responseDTO = roomClassService.getRoomClassById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<RoomClassResponse>> getAllRoomClassesByHotel(@PathVariable Integer hotelId) {
        List<RoomClassResponse> responseDTOs = roomClassService.getAllRoomClassesByHotel(hotelId);
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomClassResponse> updateRoomClass(@PathVariable Integer id, @Valid @RequestBody RoomClassRequest requestDTO) {
        RoomClassResponse responseDTO = roomClassService.updateRoomClass(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomClass(@PathVariable Integer id) {
        roomClassService.deleteRoomClass(id);
        return ResponseEntity.noContent().build();
    }
}