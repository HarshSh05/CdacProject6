package com.taskflow.service;

import com.taskflow.entity.Task;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeadlineScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    // Runs every 15 minutes — checks for tasks due within 24 hours or already overdue
    @Scheduled(fixedRate = 900000)
    public void checkDeadlines() {
        LocalDateTime threshold = LocalDateTime.now().plusHours(24);
        List<Task> dueSoon = taskRepository.findOverdueOrDueSoon(threshold);

        for (Task task : dueSoon) {
            if (task.getAssignee() != null) {
                boolean isOverdue = task.getDueDate().isBefore(LocalDateTime.now());
                String message = isOverdue
                        ? "Task overdue: " + task.getTitle()
                        : "Task due in 24 hours: " + task.getTitle();
                notificationService.notify(task.getAssignee(), message);
                log.info("Deadline notification sent for task: {}", task.getTitle());
            }
        }
    }
}
