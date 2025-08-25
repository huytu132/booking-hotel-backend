package com.example.identity_service.repository;

import com.example.identity_service.entity.RefreshToken;
import com.example.identity_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    // Thay Optional<List<...>> thành List<...> cho đơn giản
    List<RefreshToken> findAllByUser(User user);
}
