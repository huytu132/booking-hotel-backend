package com.example.identity_service.controller;

import com.example.identity_service.dto.request.BedTypeRequest;
import com.example.identity_service.dto.response.BedTypeResponse;
import com.example.identity_service.service.BedTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bed-types")
@RequiredArgsConstructor
public class BedTypeController {

    private final BedTypeService bedTypeService;

    @PostMapping
    public ResponseEntity<BedTypeResponse> createBedType(@Valid @RequestBody BedTypeRequest requestDTO) {
        BedTypeResponse responseDTO = bedTypeService.createBedType(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BedTypeResponse> getBedTypeById(@PathVariable Integer id) {
        BedTypeResponse responseDTO = bedTypeService.getBedTypeById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<BedTypeResponse>> getAllBedTypes() {
        List<BedTypeResponse> responseDTOs = bedTypeService.getAllBedTypes();
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BedTypeResponse> updateBedType(@PathVariable Integer id, @Valid @RequestBody BedTypeRequest requestDTO) {
        BedTypeResponse responseDTO = bedTypeService.updateBedType(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBedType(@PathVariable Integer id) {
        bedTypeService.deleteBedType(id);
        return ResponseEntity.noContent().build();
    }
}