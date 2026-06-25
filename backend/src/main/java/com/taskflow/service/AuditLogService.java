package com.taskflow.service;

import com.taskflow.entity.AuditLog;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(User actor, Project project, String eventType, String entityType,
                    Long entityId, String oldValue, String newValue) {
        AuditLog log = AuditLog.builder()
                .actor(actor)
                .project(project)
                .eventType(eventType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> getLogsForEntity(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                entityType, entityId, pageable);
    }
}