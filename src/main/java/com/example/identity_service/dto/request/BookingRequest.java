package com.example.identity_service.dto.request;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private List<BookingRoomRequest> rooms;
}