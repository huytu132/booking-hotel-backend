package com.example.identity_service.service.notification;

import com.example.identity_service.configuration.RabbitMQConfig;
import com.example.identity_service.dto.request.SendNotificationRequest;
import com.example.identity_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(SendNotificationRequest request) {
        log.info("Received notification from RabbitMQ: userId={}, type={}", request.getUserId(), request.getType());
        try {
            notificationService.createAndPush(request);
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage(), e);
            // Có thể implement dead letter queue sau nếu cần retry
        }
    }
}
