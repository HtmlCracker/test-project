package com.example.securityservice.repository;

import com.example.securityservice.entity.PasswordResetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordResetEntity, UUID> {
    Optional<PasswordResetEntity> findByToken(UUID token);
    Optional<PasswordResetEntity> findByEmail(String email);
}
