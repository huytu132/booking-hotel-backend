package com.example.identity_service.dto.request;

import com.example.identity_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendNotificationRequest implements Serializable {
    private int userId;
    private NotificationType type;
    private String title;
    private String message;
    private Integer referenceId; // nullable
}
