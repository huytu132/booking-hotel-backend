package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import com.example.identity_service.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Tổng số phòng trong đơn
    @Column(name = "total_room", nullable = false)
    private Integer totalRoom;

    // Tổng số tiền của đơn
    @Column(name = "booking_amount", precision = 10, scale = 2)
    private BigDecimal bookingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    // Quan hệ
    @Builder.Default
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingRoom> bookingRooms = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();
}
