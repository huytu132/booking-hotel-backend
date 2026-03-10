package com.example.identity_service.controller;

import com.example.identity_service.dto.request.ApiResponse;
import com.example.identity_service.dto.request.SendNotificationRequest;
import com.example.identity_service.dto.response.NotificationCountResponse;
import com.example.identity_service.dto.response.NotificationResponse;
import com.example.identity_service.service.NotificationService;
import com.example.identity_service.service.notification.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    /**
     * Lấy danh sách notification (phân trang)
     */
    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @RequestParam int userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> result = notificationService.getNotifications(userId, pageable);

        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(result)
                .build();
    }

    /**
     * Lấy số notification chưa đọc
     */
    @GetMapping("/unread-count")
    public ApiResponse<NotificationCountResponse> getUnreadCount(@RequestParam int userId) {
        NotificationCountResponse result = notificationService.getUnreadCount(userId);

        return ApiResponse.<NotificationCountResponse>builder()
                .result(result)
                .build();
    }

    /**
     * Đánh dấu 1 notification đã đọc
     */
    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @RequestParam int userId,
            @PathVariable int id) {

        notificationService.markAsRead(userId, id);

        return ApiResponse.<Void>builder()
                .message("Notification marked as read")
                .build();
    }

    /**
     * Đánh dấu tất cả notification đã đọc
     */
    @PutMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@RequestParam int userId) {
        notificationService.markAllAsRead(userId);

        return ApiResponse.<Void>builder()
                .message("All notifications marked as read")
                .build();
    }

    /**
     * [DEV ONLY] API test gửi notification qua RabbitMQ
     */
    @PostMapping("/test-send")
    public ApiResponse<Void> testSendNotification(@RequestBody SendNotificationRequest request) {
        notificationProducer.send(request);

        return ApiResponse.<Void>builder()
                .message("Notification sent to RabbitMQ")
                .build();
    }
}
