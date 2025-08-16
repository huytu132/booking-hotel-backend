package com.example.identity_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.identity_service.dto.request.HotelRequest;
import com.example.identity_service.dto.response.HotelResponse;
import com.example.identity_service.entity.Hotel;
import com.example.identity_service.mapper.HotelMapper;
import com.example.identity_service.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final Cloudinary cloudinary;
    private final HotelMapper hotelMapper;

    // CREATE
    public HotelResponse createHotel(HotelRequest request) throws IOException {
        Hotel hotel = hotelMapper.toEntity(request);

        MultipartFile file = request.getImg();
        if (file != null && !file.isEmpty()) {
            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            hotel.setImageUrl(uploadResult.get("secure_url").toString());
        }

        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toDto(saved);
    }

    // READ ALL
    public List<HotelResponse> getAllHotels() {
        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toDto)
                .collect(Collectors.toList());
    }

    // READ BY ID
    public HotelResponse getHotelById(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id " + id));
        return hotelMapper.toDto(hotel);
    }

    // UPDATE
    public HotelResponse updateHotel(Integer id, HotelRequest request) throws IOException {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id " + id));

        hotel.setHotelName(request.getHotelName());
        hotel.setLocation(request.getLocation());
        hotel.setDescription(request.getDescription());

        MultipartFile file = request.getImg();
        if (file != null && !file.isEmpty()) {
            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            hotel.setImageUrl(uploadResult.get("secure_url").toString());
        }

        Hotel updated = hotelRepository.save(hotel);
        return hotelMapper.toDto(updated);
    }

    // DELETE
    public void deleteHotel(Integer id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found with id " + id));
        hotelRepository.delete(hotel);
    }

    // GET ALL LOCATIONS
    public List<String> getAllLocations() {
        return hotelRepository.findDistinctLocations();
    }

    // SEARCH
    public List<HotelResponse> searchHotels(String name, String location) {
        return hotelRepository.searchHotels(name, location).stream()
                .map(hotelMapper::toDto)
                .toList();
    }
}
