package org.example.api.repositories;

import org.example.api.entities.EmailQueneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.UUID;

public interface EmailQueneRepository extends JpaRepository<EmailQueneEntity, UUID> {
    @Query(value = "SELECT * FROM email_quene ORDER BY id LIMIT ?1", nativeQuery = true)
    ArrayList<EmailQueneEntity> findTopNRecords(int limit);
}
