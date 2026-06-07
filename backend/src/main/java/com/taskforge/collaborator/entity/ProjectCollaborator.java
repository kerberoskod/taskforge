package com.taskforge.collaborator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "project_collaborators",
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectCollaborator {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProjectCollaborator() {}

    public ProjectCollaborator(UUID projectId, UUID userId, String role) {
        this.id = UUID.randomUUID();
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getProjectId() { return projectId; }
    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
