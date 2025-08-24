package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "addon")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Addon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "addon_name", nullable = false)
    private String addonName;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "room_class_addon",
            joinColumns = @JoinColumn(name = "addon_id"),
            inverseJoinColumns = @JoinColumn(name = "room_class_id"))
    private List<RoomClass> roomClasses = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "addon", fetch = FetchType.LAZY)
    private List<BookingRoomClassAddon> bookingRoomClassAddons = new ArrayList<>();
}