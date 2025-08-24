package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import com.example.identity_service.enums.RoomStatusType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_class_id", nullable = false)
    private RoomClass roomClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status_id")
    private RoomStatusType roomStatus;

    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @JsonIgnore
    @ManyToMany(mappedBy = "rooms", fetch = FetchType.LAZY)
    private List<BookingRoomClass> bookingRoomClasses = new ArrayList<>();
}
