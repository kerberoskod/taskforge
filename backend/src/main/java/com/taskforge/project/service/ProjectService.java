package com.taskforge.project.service;

import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.collaborator.service.CollaboratorService;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.project.dto.CreateProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CollaboratorService collaboratorService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, CollaboratorService collaboratorService,
                          ActivityLogService activityLogService, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.collaboratorService = collaboratorService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    public ProjectResponse create(CreateProjectRequest request, UUID ownerId) {
        Project project = new Project(request.getName(), request.getDescription(), ownerId);
        projectRepository.save(project);
        return new ProjectResponse(project);
    }

    public Page<ProjectResponse> getProjectsByOwner(UUID ownerId, Pageable pageable) {
        return projectRepository.findAccessibleProjects(ownerId, pageable)
                .map(ProjectResponse::new);
    }

    public ProjectResponse getProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(userId) && !collaboratorService.isCollaborator(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }
        return new ProjectResponse(project);
    }

    public ProjectResponse update(UUID projectId, CreateProjectRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can update this project");
        }
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);

        userRepository.findById(userId).ifPresent(user ->
                activityLogService.log(projectId, userId, user.getName(), "UPDATED", "PROJECT", projectId,
                        "Updated project: " + request.getName()));

        return new ProjectResponse(project);
    }

    public void delete(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can delete this project");
        }

        userRepository.findById(userId).ifPresent(user ->
                activityLogService.log(projectId, userId, user.getName(), "DELETED", "PROJECT", projectId,
                        "Deleted project: " + project.getName()));

        projectRepository.deleteById(projectId);
    }
}
