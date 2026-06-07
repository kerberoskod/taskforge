package com.taskforge.collaborator.service;

import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.auth.entity.User;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.collaborator.dto.CollaboratorRequest;
import com.taskforge.collaborator.dto.CollaboratorResponse;
import com.taskforge.collaborator.entity.ProjectCollaborator;
import com.taskforge.collaborator.repository.ProjectCollaboratorRepository;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CollaboratorService {

    private final ProjectCollaboratorRepository collaboratorRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public CollaboratorService(ProjectCollaboratorRepository collaboratorRepository,
                                ProjectRepository projectRepository,
                                UserRepository userRepository,
                                ActivityLogService activityLogService) {
        this.collaboratorRepository = collaboratorRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    public List<CollaboratorResponse> getCollaborators(UUID projectId, UUID currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can view collaborators");
        }
        return collaboratorRepository.findByProjectId(projectId).stream()
                .map(c -> {
                    User user = userRepository.findById(c.getUserId()).orElse(null);
                    return new CollaboratorResponse(c,
                            user != null ? user.getName() : "Unknown",
                            user != null ? user.getEmail() : "");
                })
                .toList();
    }

    public CollaboratorResponse addCollaborator(UUID projectId, CollaboratorRequest request, UUID ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can add collaborators");
        }
        if (project.getOwnerId().equals(request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot add the project owner as a collaborator");
        }
        if (collaboratorRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a collaborator");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectCollaborator collaborator = new ProjectCollaborator(projectId, request.getUserId(), request.getRole());
        collaboratorRepository.save(collaborator);

        userRepository.findById(ownerId).ifPresent(owner ->
                activityLogService.log(projectId, ownerId, owner.getName(), "ADDED_COLLABORATOR", "COLLABORATOR",
                        request.getUserId(), "Added collaborator: " + user.getName()));

        return new CollaboratorResponse(collaborator, user.getName(), user.getEmail());
    }

    public void removeCollaborator(UUID projectId, UUID userId, UUID ownerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project owner can remove collaborators");
        }
        ProjectCollaborator collaborator = collaboratorRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found"));
        collaboratorRepository.delete(collaborator);

        userRepository.findById(ownerId).ifPresent(owner ->
                userRepository.findById(userId).ifPresent(user ->
                        activityLogService.log(projectId, ownerId, owner.getName(), "REMOVED_COLLABORATOR", "COLLABORATOR",
                                userId, "Removed collaborator: " + user.getName())));
    }

    public boolean isCollaborator(UUID projectId, UUID userId) {
        return collaboratorRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    public List<UUID> getCollaboratorProjectIds(UUID userId) {
        return collaboratorRepository.findByUserId(userId).stream()
                .map(ProjectCollaborator::getProjectId)
                .toList();
    }
}
