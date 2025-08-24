package com.example.identity_service.repository;

import com.example.identity_service.entity.OAuth2Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2AccountRepository extends JpaRepository<OAuth2Account, Integer> {
}