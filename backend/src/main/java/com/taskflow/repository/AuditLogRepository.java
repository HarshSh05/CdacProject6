package com.taskflow.repository;

import com.taskflow.entity.AuditLog;
import com.taskflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByProjectOrderByCreatedAtDesc(Project project, Pageable pageable);
}