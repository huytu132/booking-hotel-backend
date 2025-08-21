package com.example.identity_service.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRoomAddonResponse {
    private Integer id;
    private Integer addonId;
    private String addonName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}