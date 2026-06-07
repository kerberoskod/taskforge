package com.taskforge.task.dto;

import com.taskforge.task.entity.Task;
import com.taskforge.task.entity.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TaskResponse {

    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private int position;
    private UUID projectId;
    private UUID assigneeId;
    private LocalDate dueDate;
    private List<UUID> labelIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.position = task.getPosition();
        this.projectId = task.getProjectId();
        this.assigneeId = task.getAssigneeId();
        this.dueDate = task.getDueDate();
        this.labelIds = List.of();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getPosition() {
        return position;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public List<UUID> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(List<UUID> labelIds) {
        this.labelIds = labelIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
