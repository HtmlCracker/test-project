package com.example.securityservice.repository;

import com.example.securityservice.entity.User;
import org.apache.kafka.common.quota.ClientQuotaAlteration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    void deleteById(UUID id);
    Optional<User> findByEmail(String email);
}
