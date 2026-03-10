package com.example.identity_service.mapper;

import com.example.identity_service.dto.response.NotificationResponse;
import com.example.identity_service.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
