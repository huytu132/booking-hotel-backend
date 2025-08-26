package com.example.identity_service.configuration;

import com.example.identity_service.enums.BookingStatus;
import com.example.identity_service.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final BookingRepository bookingRepository;
    private final StringRedisTemplate redis;

    @Override
    @Transactional
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key expired: {}", expiredKey);

        if (expiredKey.startsWith("booking:map:")) {
            String bookingIdStr = expiredKey.replace("booking:map:", "");
            Integer bookingId = Integer.valueOf(bookingIdStr);
            log.info(bookingIdStr);

            bookingRepository.findById(bookingId).ifPresent(booking -> {
                if (booking.getBookingStatus() == BookingStatus.PENDING) {
                    booking.setBookingStatus(BookingStatus.CART);

                    // set tất cả booking_room_class thành IN_CART
                    booking.getBookingRoomClasses().forEach(brc -> brc.setStatus("IN_CART"));

                    bookingRepository.save(booking);
                    log.info("Booking {} expired (map) → set CANCELLED + booking_room_class → IN_CART", bookingId);
                }
            });
        }

    }
}
