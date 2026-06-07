package com.taskforge.activity.service;

import com.taskforge.activity.dto.ActivityLogResponse;
import com.taskforge.activity.entity.ActivityLog;
import com.taskforge.activity.repository.ActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(UUID projectId, UUID userId, String userName,
                    String action, String entityType, UUID entityId, String details) {
        ActivityLog log = new ActivityLog(projectId, userId, userName, action, entityType, entityId, details);
        activityLogRepository.save(log);
    }

    public Page<ActivityLogResponse> getActivityLogs(UUID projectId, Pageable pageable) {
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(ActivityLogResponse::new);
    }
}
