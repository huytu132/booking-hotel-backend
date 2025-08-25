package com.example.identity_service.configuration;

import com.example.identity_service.enums.BookingStatus;
import com.example.identity_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final BookingRepository bookingRepository;
    private final StringRedisTemplate redis;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key expired: {}", expiredKey);

        if (expiredKey.startsWith("booking:hold:")) {
            String holdId = expiredKey.replace("booking:hold:", "");

            // Tìm bookingId chứa holdId này
            Set<String> keys = redis.keys("booking:map:*");
            if (keys != null) {
                for (String key : keys) {
                    if (Boolean.TRUE.equals(redis.opsForSet().isMember(key, holdId))) {
                        String bookingIdStr = key.replace("booking:map:", "");
                        Integer bookingId = Integer.valueOf(bookingIdStr);
                        log.info(bookingId+"");

                        bookingRepository.findById(bookingId).ifPresent(booking -> {
                            if (booking.getBookingStatus() == BookingStatus.PENDING) {
                                booking.setBookingStatus(BookingStatus.CANCELLED);
                                bookingRepository.save(booking);
                                log.info("Booking {} expired → set CANCELLED", bookingId);
                            }
                        });
                    }
                }
            }
        }
    }
}
