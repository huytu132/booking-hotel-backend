package com.example.identity_service.service;

import com.example.identity_service.dto.request.SendNotificationRequest;
import com.example.identity_service.dto.response.NotificationCountResponse;
import com.example.identity_service.dto.response.NotificationResponse;
import com.example.identity_service.entity.Notification;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.mapper.NotificationMapper;
import com.example.identity_service.repository.NotificationRepository;
import com.example.identity_service.service.cache.NotificationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationCacheService notificationCacheService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Tạo notification + lưu DB + increment Redis + push WebSocket.
     * Được gọi bởi NotificationConsumer (sau khi nhận message từ RabbitMQ).
     */
    @Transactional
    public void createAndPush(SendNotificationRequest request) {
        // 1. Lưu vào DB
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .referenceId(request.getReferenceId())
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        // 2. Increment Redis unread count
        notificationCacheService.incrementUnread(request.getUserId());

        // 3. Push realtime qua WebSocket (STOMP)
        NotificationResponse response = notificationMapper.toResponse(notification);
        String destination = "/queue/notifications/" + request.getUserId();
        messagingTemplate.convertAndSend(destination, response);

        log.info("Notification created and pushed: id={}, userId={}, type={}",
                notification.getId(), request.getUserId(), request.getType());
    }

    /**
     * Lấy danh sách notification (phân trang, mới nhất trước)
     */
    public Page<NotificationResponse> getNotifications(int userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreateAtDesc(userId, pageable)
                .map(notificationMapper::toResponse);
    }

    /**
     * Lấy số lượng thông báo chưa đọc (cache trước, fallback DB)
     */
    public NotificationCountResponse getUnreadCount(int userId) {
        Long cached = notificationCacheService.getUnreadCount(userId);

        if (cached != null) {
            return NotificationCountResponse.builder().unreadCount(cached).build();
        }

        // Cache miss → đọc DB và set cache
        long count = notificationRepository.countByUserIdAndIsRead(userId, false);
        notificationCacheService.setUnreadCount(userId, count);

        return NotificationCountResponse.builder().unreadCount(count).build();
    }

    /**
     * Đánh dấu 1 notification đã đọc
     */
    @Transactional
    public void markAsRead(int userId, int notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getUserId() != userId) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
            notificationCacheService.decrementUnread(userId);
        }
    }

    /**
     * Đánh dấu tất cả notification đã đọc
     */
    @Transactional
    public void markAllAsRead(int userId) {
        int updated = notificationRepository.markAllAsReadByUserId(userId);
        if (updated > 0) {
            notificationCacheService.resetUnread(userId);
        }
        log.info("Marked {} notifications as read for userId={}", updated, userId);
    }
}
