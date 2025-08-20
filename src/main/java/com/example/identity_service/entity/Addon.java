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

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "addon")
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

    @JsonIgnore
    @OneToMany(mappedBy = "addon", fetch = FetchType.LAZY)
    private List<BookingRoomAddon> bookingAddons = new ArrayList<>();
}
