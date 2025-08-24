package com.example.identity_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking_room_class")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingRoomClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_class_id", nullable = false)
    private RoomClass roomClass; // Liên kết với RoomClass

    @Column(name = "quantity", nullable = false)
    private int quantity; // Số lượng phòng được đặt cho RoomClass này

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_room_class_room",
            joinColumns = @JoinColumn(name = "booking_room_class_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id")
    )
    private List<Room> rooms = new ArrayList<>(); // Danh sách các phòng cụ thể, được admin gán sau

    @Column(name = "checkin_date", nullable = false)
    private LocalDateTime checkinDate;

    @Column(name = "checkout_date", nullable = false)
    private LocalDateTime checkoutDate;

    @Column(name = "status")
    private String status;

    @Column(name = "num_adults", nullable = false)
    private int numAdults; // Tổng số người lớn cho tất cả các phòng trong quantity

    @Column(name = "num_children")
    private int numChildren; // Tổng số trẻ em cho tất cả các phòng trong quantity

    @Column(name = "room_price", precision = 10, scale = 2)
    private BigDecimal roomPrice; // Giá mỗi phòng của RoomClass

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal; // Tổng tiền cho quantity phòng + addons

    @OneToMany(mappedBy = "bookingRoomClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingRoomClassAddon> bookingRoomClassAddons = new ArrayList<>();
}