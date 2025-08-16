package com.example.identity_service.controller;

import com.example.identity_service.dto.request.HotelRequest;
import com.example.identity_service.dto.response.HotelResponse;
import com.example.identity_service.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    // CREATE - ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(
            @RequestParam String hotelName,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile img
    ) throws IOException {
        HotelRequest request = HotelRequest.builder()
                .hotelName(hotelName)
                .location(location)
                .description(description)
                .img(img)
                .build();
        return ResponseEntity.ok(hotelService.createHotel(request));
    }

    // READ ALL - USER or ADMIN
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<List<HotelResponse>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    // READ BY ID
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Integer id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    // UPDATE - ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Integer id,
            @RequestParam String hotelName,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile img
    ) throws IOException {
        HotelRequest request = HotelRequest.builder()
                .hotelName(hotelName)
                .location(location)
                .description(description)
                .img(img)
                .build();
        return ResponseEntity.ok(hotelService.updateHotel(id, request));
    }

    // DELETE - ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Integer id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }

    // GET DISTINCT LOCATIONS - PUBLIC
    @GetMapping("/locations")
    public ResponseEntity<List<String>> getAllLocations() {
        return ResponseEntity.ok(hotelService.getAllLocations());
    }

    // SEARCH HOTEL BY NAME & LOCATION - PUBLIC
    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String location
    ) {
        return ResponseEntity.ok(hotelService.searchHotels(name, location));
    }
}
