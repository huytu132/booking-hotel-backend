package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "hotel")
public class Hotel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @Nationalized
    @Column(name = "hotel_name", nullable = false)
    private String hotelName;

    @Size(max = 200)
    @Nationalized
    @Column(name = "location")
    private String location;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RoomClass> roomClasses = new ArrayList<>();
}