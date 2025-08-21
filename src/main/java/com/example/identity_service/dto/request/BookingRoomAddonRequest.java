package com.example.identity_service.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRoomAddonRequest {
    private Integer addonId;
    private Integer quantity;
}