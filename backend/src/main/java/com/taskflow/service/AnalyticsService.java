package com.taskflow.service;

import com.taskflow.dto.response.AnalyticsResponse;
import com.taskflow.entity.*;
import com.taskflow.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;

    public AnalyticsResponse getProjectAnalytics(User actor, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));

        boolean isMember = projectMemberRepository.existsByProjectAndUser(project, actor);
        if (!isMember) {
            throw new SecurityException("You are not a member of this project");
        }

        // All tasks in the project
        List<Task> allTasks = taskRepository.findByProject(project);

        // Tasks grouped by column name (used as "status")
        List<BoardColumn> columns = boardColumnRepository.findByProjectOrderByPositionAsc(project);
        Map<Long, String> columnNames = columns.stream()
                .collect(Collectors.toMap(BoardColumn::getId, BoardColumn::getName));

        Map<String, Long> taskCountByStatus = allTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> columnNames.getOrDefault(t.getColumn().getId(), "Unknown"),
                        Collectors.counting()
                ));

        // Overdue tasks (dueDate in the past, not in a "Done"-like column)
        LocalDateTime now = LocalDateTime.now();
        long overdueCount = allTasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(now))
                .filter(t -> {
                    String colName = columnNames.getOrDefault(t.getColumn().getId(), "");
                    return !colName.equalsIgnoreCase("Done");
                })
                .count();

        // Tasks per member (only tasks with an assignee)
        Map<Long, List<Task>> tasksByAssignee = allTasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getId()));

        List<AnalyticsResponse.MemberTaskCount> tasksPerMember = tasksByAssignee.entrySet().stream()
                .map(e -> {
                    User assignee = e.getValue().get(0).getAssignee();
                    return AnalyticsResponse.MemberTaskCount.builder()
                            .userId(assignee.getId())
                            .userName(assignee.getName())
                            .taskCount(e.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparingLong(AnalyticsResponse.MemberTaskCount::getTaskCount).reversed())
                .collect(Collectors.toList());

        return AnalyticsResponse.builder()
                .projectId(projectId)
                .taskCountByStatus(taskCountByStatus)
                .overdueTaskCount(overdueCount)
                .tasksPerMember(tasksPerMember)
                .build();
    }
}
