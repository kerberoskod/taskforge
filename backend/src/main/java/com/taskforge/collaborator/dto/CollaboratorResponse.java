package com.taskforge.collaborator.dto;

import com.taskforge.collaborator.entity.ProjectCollaborator;

import java.time.LocalDateTime;
import java.util.UUID;

public class CollaboratorResponse {

    private UUID id;
    private UUID projectId;
    private UUID userId;
    private String userName;
    private String userEmail;
    private String role;
    private LocalDateTime createdAt;

    public CollaboratorResponse(ProjectCollaborator collaborator, String userName, String userEmail) {
        this.id = collaborator.getId();
        this.projectId = collaborator.getProjectId();
        this.userId = collaborator.getUserId();
        this.userName = userName;
        this.userEmail = userEmail;
        this.role = collaborator.getRole();
        this.createdAt = collaborator.getCreatedAt();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
