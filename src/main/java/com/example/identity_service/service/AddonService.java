package com.example.identity_service.service;

import com.example.identity_service.dto.request.AddonRequest;
import com.example.identity_service.dto.response.AddonResponse;
import com.example.identity_service.entity.Addon;
import com.example.identity_service.mapper.AddonMapper;
import com.example.identity_service.repository.AddonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddonService {

    private final AddonRepository addonRepository;
    private final AddonMapper addonMapper;

    public List<AddonResponse> getAll() {
        return addonRepository.findAll()
                .stream().map(addonMapper::toResponse).toList();
    }

    public AddonResponse getById(int id) {
        return addonRepository.findById(id)
                .map(addonMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Addon not found"));
    }

    public AddonResponse create(AddonRequest request) {
        Addon addon = addonMapper.toEntity(request);
        return addonMapper.toResponse(addonRepository.save(addon));
    }

    public AddonResponse update(int id, AddonRequest request) {
        Addon addon = addonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Addon not found"));
        addonMapper.updateEntityFromRequest(request, addon);
        return addonMapper.toResponse(addonRepository.save(addon));
    }

    public void delete(int id) {
        addonRepository.deleteById(id);
    }
}
