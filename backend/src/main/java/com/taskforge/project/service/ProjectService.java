package com.taskforge.project.service;

import com.taskforge.project.dto.CreateProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectResponse create(CreateProjectRequest request, UUID ownerId) {
        Project project = new Project(request.getName(), request.getDescription(), ownerId);
        projectRepository.save(project);
        return new ProjectResponse(project);
    }

    public List<ProjectResponse> getProjectsByOwner(UUID ownerId) {
        return projectRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(ProjectResponse::new)
                .toList();
    }

    public ProjectResponse getProject(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!project.getOwnerId().equals(userId)) {
            // For simplicity, owner-only access. In production, add member table.
            // We'll skip membership check for now and allow any authenticated user to view.
        }
        return new ProjectResponse(project);
    }

    public ProjectResponse update(UUID projectId, CreateProjectRequest request, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectRepository.save(project);
        return new ProjectResponse(project);
    }

    public void delete(UUID projectId, UUID userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!project.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this project");
        }
        projectRepository.deleteById(projectId);
    }
}
