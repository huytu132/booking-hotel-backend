package com.example.identity_service.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRoomResponse {
    private Integer id;
    private Integer roomId;
    private String roomNumber;
    private String roomClassName;
    private String hotelName;
    private LocalDateTime checkinDate;
    private LocalDateTime checkoutDate;
    private String status;
    private Integer numAdults;
    private Integer numChildren;
    private BigDecimal roomPrice;
    private BigDecimal subtotal;
    private List<BookingRoomAddonResponse> addons;
}