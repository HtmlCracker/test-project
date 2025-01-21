package com.example.securityservice.repository;

import com.example.securityservice.entity.RefreshToken;
import com.example.securityservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
