package com.example.identity_service.entity;

import com.example.identity_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users") // số nhiều để tránh conflict keyword
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Email
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 100)
    @Column // Cho phép null nếu chỉ dùng OAuth2
    private String password;

    @Size(max = 50)
    @Nationalized
    private String firstName;

    @Size(max = 50)
    @Nationalized
    private String lastName;

    @Size(max = 50)
    @Column(unique = true)
    private String phoneNo;

    @Column(length = 20)
    private String provider;

    /** Xác thực email? */
    @Column(nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<Role> roles = new HashSet<>();

    /** Một user có thể có nhiều tài khoản OAuth2 */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OAuth2Account> oauth2Accounts = new HashSet<>();

    /** Một user có thể có nhiều token xác thực (verify/reset) */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VerificationToken> verificationTokens = new HashSet<>();
}
