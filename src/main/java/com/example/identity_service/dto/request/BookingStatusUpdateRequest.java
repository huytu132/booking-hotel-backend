package com.example.identity_service.dto.request;

import com.example.identity_service.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusUpdateRequest {
    private BookingStatus status;
    private String note; // Ghi chú khi update status
}