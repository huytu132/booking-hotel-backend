package com.example.identity_service.dto.request;

import com.example.identity_service.enums.RoomStatusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomRequest {
    @NotNull(message = "Room class ID is required")
    private Integer roomClassId;

    @NotNull(message = "Room status is required")
    private RoomStatusType roomStatus;

    @NotBlank(message = "Room number is required")
    @Size(max = 50, message = "Room number must not exceed 50 characters")
    private String roomNumber;
}
