package com.example.identity_service.dto.response;

import com.example.identity_service.enums.BookingStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Integer id;
    private Integer userId;
    private String userEmail;
    private String userFullName;
    private Integer totalRoom;
    private BigDecimal bookingAmount;
    private BookingStatus bookingStatus;
    private List<BookingRoomResponse> bookingRoomResponses;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
}