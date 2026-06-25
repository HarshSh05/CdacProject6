package com.taskflow.controller;

import com.taskflow.dto.response.AuditLogResponse;
import com.taskflow.entity.AuditLog;
import com.taskflow.entity.User;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AuditLogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.taskflow.repository.AuditLogRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/audit")
@RequiredArgsConstructor
public class AuditLogController {

        private final AuditLogService auditLogService;
        private final ProjectRepository projectRepository;
        private final ProjectMemberRepository projectMemberRepository;
        private final UserRepository userRepository;
        private final AuditLogRepository auditLogRepository;

        /**
         * TC1 (Person 5) — Paginated audit log for all TASK events in a project
         * GET /api/v1/projects/{projectId}/audit?page=0&size=20
         */
        @GetMapping
        public ResponseEntity<AuditLogResponse> getAuditLog(
                        @PathVariable Long projectId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @AuthenticationPrincipal UserDetails principal) {

                User actor = resolveUser(principal);

                var project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

                boolean isMember = projectMemberRepository.existsByProjectAndUser(project, actor);
                if (!isMember) {
                        throw new SecurityException("You are not a member of this project");
                }

                // Query directly by project_id
                Page<AuditLog> logsPage = auditLogRepository
                                .findByProjectOrderByCreatedAtDesc(project, PageRequest.of(page, size));

                List<AuditLogResponse.AuditEventResponse> events = logsPage.getContent().stream()
                                .map(this::toEventResponse)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(AuditLogResponse.builder()
                                .page(page)
                                .totalEvents(logsPage.getTotalElements())
                                .events(events)
                                .build());
        }

        private AuditLogResponse.AuditEventResponse toEventResponse(AuditLog log) {
                return AuditLogResponse.AuditEventResponse.builder()
                                .id(log.getId())
                                .eventType(log.getEventType())
                                .actorId(log.getActor().getId())
                                .entityType(log.getEntityType())
                                .entityId(log.getEntityId())
                                .oldValue(log.getOldValue())
                                .newValue(log.getNewValue())
                                .createdAt(log.getCreatedAt())
                                .build();
        }

        private User resolveUser(UserDetails principal) {
                return userRepository.findByEmail(principal.getUsername())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Authenticated user not found: " + principal.getUsername()));
        }
}
