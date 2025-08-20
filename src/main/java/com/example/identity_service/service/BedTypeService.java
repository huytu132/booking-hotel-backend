package com.example.identity_service.service;

import com.example.identity_service.dto.request.BedTypeRequest;
import com.example.identity_service.dto.response.BedTypeResponse;
import com.example.identity_service.entity.BedType;
import com.example.identity_service.mapper.BedTypeMapper;
import com.example.identity_service.repository.BedTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BedTypeService {

    private final BedTypeRepository bedTypeRepository;
    private final BedTypeMapper bedTypeMapper;

    public BedTypeResponse createBedType(BedTypeRequest requestDTO) {
        if (bedTypeRepository.existsByBedName(requestDTO.getBedName())) {
            throw new RuntimeException("Bed type with name " + requestDTO.getBedName() + " already exists");
        }
        BedType bedType = bedTypeMapper.toEntity(requestDTO);
        bedType = bedTypeRepository.save(bedType);
        return bedTypeMapper.toResponse(bedType);
    }

    public BedTypeResponse getBedTypeById(Integer id) {
        BedType bedType = bedTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed type not found with id: " + id));
        return bedTypeMapper.toResponse(bedType);
    }

    public List<BedTypeResponse> getAllBedTypes() {
        return bedTypeRepository.findAll().stream()
                .map(bedTypeMapper::toResponse)
                .collect(Collectors.toList());
    }

    public BedTypeResponse updateBedType(Integer id, BedTypeRequest requestDTO) {
        BedType existingBedType = bedTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bed type not found with id: " + id));

        if (!existingBedType.getBedName().equals(requestDTO.getBedName()) &&
                bedTypeRepository.existsByBedName(requestDTO.getBedName())) {
            throw new RuntimeException("Bed type with name " + requestDTO.getBedName() + " already exists");
        }

        bedTypeMapper.updateEntityFromRequest(requestDTO, existingBedType);
        bedTypeRepository.save(existingBedType);
        return bedTypeMapper.toResponse(existingBedType);
    }

    public void deleteBedType(Integer id) {
        if (!bedTypeRepository.existsById(id)) {
            throw new RuntimeException("Bed type not found with id: " + id);
        }
        bedTypeRepository.deleteById(id);
    }
}
