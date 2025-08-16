package com.example.identity_service.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelResponse {
    private Integer id;
    private String hotelName;
    private String location;
    private String description;
    private String imageUrl;
}
