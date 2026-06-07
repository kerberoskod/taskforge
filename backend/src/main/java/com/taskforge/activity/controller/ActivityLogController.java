package com.taskforge.activity.controller;

import com.taskforge.activity.dto.ActivityLogResponse;
import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.collaborator.service.CollaboratorService;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/activity")
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final ProjectRepository projectRepository;
    private final CollaboratorService collaboratorService;

    public ActivityLogController(ActivityLogService activityLogService,
                                  ProjectRepository projectRepository,
                                  CollaboratorService collaboratorService) {
        this.activityLogService = activityLogService;
        this.projectRepository = projectRepository;
        this.collaboratorService = collaboratorService;
    }

    @GetMapping
    public ResponseEntity<Page<ActivityLogResponse>> getActivityLogs(@PathVariable UUID projectId,
                                                                      @AuthenticationPrincipal UUID userId,
                                                                      @PageableDefault(size = 50) Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(userId) && !collaboratorService.isCollaborator(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this project");
        }
        return ResponseEntity.ok(activityLogService.getActivityLogs(projectId, pageable));
    }
}
