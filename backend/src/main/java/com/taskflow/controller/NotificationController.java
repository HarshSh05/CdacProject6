package com.taskflow.controller;

import com.taskflow.entity.Notification;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import com.taskflow.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * TC3 (Person 5) — List unread notifications for the authenticated user
     * GET /api/v1/users/me/notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @AuthenticationPrincipal UserDetails principal) {

        User actor = resolveUser(principal);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(actor));
    }

    /**
     * Mark all notifications as read
     * PATCH /api/v1/users/me/notifications/read
     */
    @PatchMapping("/read")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal UserDetails principal) {

        User actor = resolveUser(principal);
        notificationService.markAllRead(actor);
        return ResponseEntity.noContent().build();
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Authenticated user not found: " + principal.getUsername()));
    }
}
