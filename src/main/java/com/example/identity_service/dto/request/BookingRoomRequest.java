package com.example.identity_service.dto.request;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRoomRequest {
    private Integer roomId;
    private LocalDateTime checkinDate;
    private LocalDateTime checkoutDate;
    private Integer numAdults;
    private Integer numChildren;
    private List<BookingRoomAddonRequest> addons;
}