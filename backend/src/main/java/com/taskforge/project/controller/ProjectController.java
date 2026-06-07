package com.taskforge.project.controller;

import com.taskforge.project.dto.CreateProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import com.taskforge.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(projectService.getProjectsByOwner(userId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request,
                                                          @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request, userId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID projectId,
                                                       @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(projectService.getProject(projectId, userId));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID projectId,
                                                          @Valid @RequestBody CreateProjectRequest request,
                                                          @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(projectService.update(projectId, request, userId));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId,
                                               @AuthenticationPrincipal UUID userId) {
        projectService.delete(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
