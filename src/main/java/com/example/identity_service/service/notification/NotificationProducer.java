package com.example.identity_service.service.notification;

import com.example.identity_service.configuration.RabbitMQConfig;
import com.example.identity_service.dto.request.SendNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Gửi notification request vào RabbitMQ.
     * Các service khác (BookingService, PaymentService...) sẽ inject class này để gửi notification.
     */
    public void send(SendNotificationRequest request) {
        log.info("Sending notification to RabbitMQ: userId={}, type={}", request.getUserId(), request.getType());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                request
        );
    }
}
