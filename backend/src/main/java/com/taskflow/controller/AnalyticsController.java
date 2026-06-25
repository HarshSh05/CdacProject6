package com.taskflow.controller;

import com.taskflow.dto.response.AnalyticsResponse;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.AnalyticsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    /**
     * TC2 (Person 5) — Project analytics
     * GET /api/v1/projects/{projectId}/analytics
     *
     * Returns:
     *   - taskCountByStatus (grouped by column name)
     *   - overdueTaskCount
     *   - tasksPerMember (sorted by task count desc)
     */
    @GetMapping
    public ResponseEntity<AnalyticsResponse> getAnalytics(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails principal) {

        User actor = resolveUser(principal);
        return ResponseEntity.ok(analyticsService.getProjectAnalytics(actor, projectId));
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Authenticated user not found: " + principal.getUsername()));
    }
}
