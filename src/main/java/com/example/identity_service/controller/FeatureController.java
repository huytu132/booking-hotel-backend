package com.example.identity_service.controller;

import com.example.identity_service.dto.request.FeatureRequest;
import com.example.identity_service.dto.response.FeatureResponse;
import com.example.identity_service.service.FeatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

    @PostMapping
    public ResponseEntity<FeatureResponse> createFeature(@Valid @RequestBody FeatureRequest requestDTO) {
        FeatureResponse responseDTO = featureService.createFeature(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureResponse> getFeatureById(@PathVariable Integer id) {
        FeatureResponse responseDTO = featureService.getFeatureById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<FeatureResponse>> getAllFeatures() {
        List<FeatureResponse> responseDTOs = featureService.getAllFeatures();
        return ResponseEntity.ok(responseDTOs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureResponse> updateFeature(@PathVariable Integer id, @Valid @RequestBody FeatureRequest requestDTO) {
        FeatureResponse responseDTO = featureService.updateFeature(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeature(@PathVariable Integer id) {
        featureService.deleteFeature(id);
        return ResponseEntity.noContent().build();
    }
}