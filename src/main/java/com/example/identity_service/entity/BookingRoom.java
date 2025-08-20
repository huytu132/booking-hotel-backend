package com.example.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_room")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Ngày check-in / check-out riêng cho phòng này
    @Column(name = "checkin_date", nullable = false)
    private LocalDateTime checkinDate;

    @Column(name = "checkout_date", nullable = false)
    private LocalDateTime checkoutDate;

    @Column(name = "status")
    private String status;

    // Số người
    @Column(name = "num_adults", nullable = false)
    private int numAdults;

    @Column(name = "num_children")
    private int numChildren;

    // Giá và phụ phí
    @Column(name = "room_price", precision = 10, scale = 2)
    private BigDecimal roomPrice;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    // Quan hệ Addon
    @OneToMany(mappedBy = "bookingRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingRoomAddon> bookingRoomAddons = new ArrayList<>();
}

