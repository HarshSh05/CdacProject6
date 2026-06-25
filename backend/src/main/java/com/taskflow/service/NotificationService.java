package com.taskflow.service;

import com.taskflow.entity.Notification;
import com.taskflow.entity.User;
import com.taskflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Create and push a notification to a specific user via WebSocket
    public void notify(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .read(false)
                .build();
        notificationRepository.save(notification);

        // Push to user's private WebSocket queue — only they receive this
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notifications",
                notification
        );
    }

    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
    }

    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndReadFalseOrderByCreatedAtDesc(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
