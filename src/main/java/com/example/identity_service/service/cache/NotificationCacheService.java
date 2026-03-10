package com.example.identity_service.service.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCacheService {

    private final StringRedisTemplate redis;

    private static final String UNREAD_KEY_PREFIX = "notification:unread:";

    private String unreadKey(int userId) {
        return UNREAD_KEY_PREFIX + userId;
    }

    /**
     * Tăng số lượng chưa đọc lên 1
     */
    public void incrementUnread(int userId) {
        redis.opsForValue().increment(unreadKey(userId));
    }

    /**
     * Giảm số lượng chưa đọc đi 1 (min 0)
     */
    public void decrementUnread(int userId) {
        String key = unreadKey(userId);
        Long current = redis.opsForValue().decrement(key);
        if (current != null && current < 0) {
            redis.opsForValue().set(key, "0");
        }
    }

    /**
     * Lấy số lượng chưa đọc (từ cache).
     * Trả null nếu key không tồn tại (để caller biết cần fallback DB).
     */
    public Long getUnreadCount(int userId) {
        String val = redis.opsForValue().get(unreadKey(userId));
        if (val == null) return null;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Set giá trị cache (VD: sau khi đọc từ DB)
     */
    public void setUnreadCount(int userId, long count) {
        redis.opsForValue().set(unreadKey(userId), String.valueOf(count));
    }

    /**
     * Reset cache (VD: sau mark all as read)
     */
    public void resetUnread(int userId) {
        redis.opsForValue().set(unreadKey(userId), "0");
    }
}
