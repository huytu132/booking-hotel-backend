package com.example.identity_service.dto.response;

import com.example.identity_service.enums.RoomStatusType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private Integer id;
    private Integer roomClassId;
    private RoomStatusType roomStatus;
    private String roomNumber;
    private LocalDateTime createAt;
    private String createBy;
    private LocalDateTime updateAt;
    private String updateBy;
}
