package com.taskflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {

    private Long projectId;
    private Map<String, Long> taskCountByStatus;
    private long overdueTaskCount;
    private List<MemberTaskCount> tasksPerMember;

    @Data
    @Builder
    public static class MemberTaskCount {
        private Long userId;
        private String userName;
        private long taskCount;
    }
}
