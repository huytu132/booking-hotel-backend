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
    private Integer roomClassId;
    private String roomClassName;
    private Integer quantity; // Số lượng phòng
    private List<Integer> roomIds;
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