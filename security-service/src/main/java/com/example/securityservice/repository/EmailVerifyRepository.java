package com.example.securityservice.repository;

import com.example.securityservice.entity.EmailVerifyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerifyRepository extends JpaRepository<EmailVerifyEntity, UUID> {
    Optional<EmailVerifyEntity> findByToken(UUID token);
}
