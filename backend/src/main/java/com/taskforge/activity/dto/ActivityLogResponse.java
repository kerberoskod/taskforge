package com.taskforge.activity.dto;

import com.taskforge.activity.entity.ActivityLog;

import java.time.LocalDateTime;
import java.util.UUID;

public class ActivityLogResponse {

    private UUID id;
    private UUID projectId;
    private UUID userId;
    private String userName;
    private String action;
    private String entityType;
    private UUID entityId;
    private String details;
    private LocalDateTime createdAt;

    public ActivityLogResponse(ActivityLog log) {
        this.id = log.getId();
        this.projectId = log.getProjectId();
        this.userId = log.getUserId();
        this.userName = log.getUserName();
        this.action = log.getAction();
        this.entityType = log.getEntityType();
        this.entityId = log.getEntityId();
        this.details = log.getDetails();
        this.createdAt = log.getCreatedAt();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
