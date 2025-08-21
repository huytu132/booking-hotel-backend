package com.example.identity_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Integer bookingId;
    private Integer userId;
    private List<BookingRoomResponse> items;
    private BigDecimal totalAmount;
    private Integer totalRooms;
}