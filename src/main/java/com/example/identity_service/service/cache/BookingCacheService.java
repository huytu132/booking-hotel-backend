package com.example.identity_service.service.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BookingCacheService {

    private final StringRedisTemplate redis;
    private static final Duration HOLD_TTL = Duration.ofMinutes(1);

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private String holdKey(String holdId) {
        return "booking:hold:" + holdId;
    }
    private String indexKey(Integer roomClassId) {
        return "booking:index:" + roomClassId;
    }
    private String bookingMapKey(Integer bookingId) {
        return "booking:map:" + bookingId;
    }

    private boolean isOverlap(LocalDateTime aStart, LocalDateTime aEnd,
                              LocalDateTime bStart, LocalDateTime bEnd) {
        // Overlap khi aStart < bEnd && aEnd > bStart
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    /**
     * Tạo hold cho 1 booking-roomClass, trả về holdId
     */
    public String holdRoomsForBooking(Integer bookingId,
                                      Integer roomClassId,
                                      LocalDateTime checkin,
                                      LocalDateTime checkout,
                                      int quantity) {
        String holdId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        long expiresAt = now + HOLD_TTL.toMillis();

        // 1) Lưu chi tiết hold (HASH) + TTL
        Map<String, String> fields = new HashMap<>();
        fields.put("roomClassId", roomClassId.toString());
        fields.put("checkin", checkin.format(ISO));
        fields.put("checkout", checkout.format(ISO));
        fields.put("quantity", Integer.toString(quantity));
        fields.put("expiresAt", Long.toString(expiresAt));

        String hKey = holdKey(holdId);
        redis.opsForHash().putAll(hKey, fields);
        redis.expire(hKey, HOLD_TTL);

        // 2) Thêm vào ZSET index theo roomClassId
        redis.opsForZSet().add(indexKey(roomClassId), holdId, expiresAt);

        // 3) Gắn vào map booking → holds (để cleanup theo booking)
        redis.opsForSet().add(bookingMapKey(bookingId), holdId);
        redis.expire(bookingMapKey(bookingId), HOLD_TTL); // map tự hết hạn theo vòng đời

        return holdId;
    }

    /**
     * Tổng số phòng đang hold (chưa hết TTL) cho roomClassId
     * và OVERLAP với [checkin, checkout)
     */
    public int getHoldingOverlap(Integer roomClassId,
                                 LocalDateTime checkin,
                                 LocalDateTime checkout) {
        String idxKey = indexKey(roomClassId);
        long nowEpoch = System.currentTimeMillis();

        // Lấy các hold chưa hết hạn: score ∈ [now, +inf]
        Set<String> activeHoldIds = redis.opsForZSet()
                .rangeByScore(idxKey, nowEpoch, Double.POSITIVE_INFINITY);

        if (activeHoldIds == null || activeHoldIds.isEmpty()) return 0;

        int sum = 0;
        for (String holdId : activeHoldIds) {
            Map<Object, Object> h = redis.opsForHash().entries(holdKey(holdId));
            if (h == null || h.isEmpty()) {
                // Hash hết TTL nhưng index chưa dọn -> remove index rác
                redis.opsForZSet().remove(idxKey, holdId);
                continue;
            }
            try {
                LocalDateTime cIn  = LocalDateTime.parse((String) h.get("checkin"), ISO);
                LocalDateTime cOut = LocalDateTime.parse((String) h.get("checkout"), ISO);
                int qty = Integer.parseInt((String) h.get("quantity"));
                if (isOverlap(checkin, checkout, cIn, cOut)) {
                    sum += qty;
                }
            } catch (Exception ignore) {
                // Nếu dữ liệu bẩn, bỏ qua & cố gắng dọn
                redis.opsForZSet().remove(idxKey, holdId);
            }
        }
        return sum;
    }

    /**
     * Giải phóng 1 hold (khi thanh toán thành công/hủy)
     */
    public void releaseHold(String holdId) {
        String hKey = holdKey(holdId);
        Map<Object, Object> h = redis.opsForHash().entries(hKey);
        if (h != null && !h.isEmpty()) {
            Integer roomClassId = Integer.valueOf((String) h.get("roomClassId"));
            // Xóa khỏi index
            redis.opsForZSet().remove(indexKey(roomClassId), holdId);
        }
        // Xóa hash
        redis.delete(hKey);
    }

    /**
     * Giải phóng toàn bộ hold gắn với 1 booking
     */
    public void releaseHoldsForBooking(Integer bookingId) {
        String mapKey = bookingMapKey(bookingId);
        Set<String> ids = redis.opsForSet().members(mapKey);
        if (ids != null) {
            for (String id : ids) releaseHold(id);
        }
        redis.delete(mapKey);
    }
}

