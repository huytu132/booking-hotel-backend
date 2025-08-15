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
@Table(name = "room_class")
public class RoomClass extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Nationalized
    @Column(name = "room_class_name", nullable = false)
    private String roomClassName;

    @Column(name = "quantity", nullable = false)
    private String quantity;

    @Column(name = "price_original", precision = 10, scale = 2)
    private BigDecimal priceOriginal;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description")
    private String description;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @JsonIgnore
    @OneToMany(mappedBy = "roomClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Room> rooms = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "room_class_bed_type",
            joinColumns = @JoinColumn(name = "room_class_id"),
            inverseJoinColumns = @JoinColumn(name = "bed_type_id"))
    private List<BedType> bedTypes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "room_class_feature",
            joinColumns = @JoinColumn(name = "room_class_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id"))
    private List<Feature> features = new ArrayList<>();

    @OneToMany(mappedBy = "roomClass", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RoomImage> roomImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
}
