package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import com.example.identity_service.enums.EnumRole;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Nationalized;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @Email
    @Size(max = 50)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Size(max = 100)
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 50)
    @Nationalized
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 50)
    @Nationalized
    @Column(name = "last_name")
    private String lastName;

    @Size(max = 50)
    @Column(name = "phone_no", unique = true)
    private String phoneNo;

    @Column(name = "roles")
    @ManyToMany
    private Set<Role> roles;
}
