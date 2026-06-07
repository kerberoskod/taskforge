package com.taskforge.collaborator.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class CollaboratorRequest {

    @NotBlank
    private UUID userId;

    private String role = "MEMBER";

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
