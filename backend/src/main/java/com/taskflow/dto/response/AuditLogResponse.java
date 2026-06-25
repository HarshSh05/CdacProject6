package com.taskflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AuditLogResponse {

    private int page;
    private long totalEvents;
    private List<AuditEventResponse> events;

    @Data
    @Builder
    public static class AuditEventResponse {
        private Long id;
        private String eventType;
        private Long actorId;
        private String entityType;
        private Long entityId;
        private Object oldValue;
        private Object newValue;
        private LocalDateTime createdAt;
    }
}
