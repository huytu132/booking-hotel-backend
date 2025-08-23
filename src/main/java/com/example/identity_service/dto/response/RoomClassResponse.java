package com.example.identity_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomClassResponse {
    private Integer id;
    private String roomClassName;
    private String quantity;
    private BigDecimal priceOriginal;
    private String description;
    private Integer discountPercent;
    private Integer capacity;
    private HotelResponse hotel;
    private List<BedTypeResponse> bedTypes;
    private List<FeatureResponse> features;
    private List<String> roomImagePaths;
    private LocalDateTime createAt;
    private String createBy;
    private LocalDateTime updateAt;
    private String updateBy;
}
