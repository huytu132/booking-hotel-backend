package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "oauth2_account",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_id"})
        }
)
public class OAuth2Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Provider như: GOOGLE, FACEBOOK, GITHUB... */
    @Column(nullable = false, length = 20)
    private String provider;

    /** ID/sub trả về từ provider */
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    /** User liên kết */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

