package com.taskforge.task.controller;

import com.taskforge.task.dto.*;
import com.taskforge.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getTasks(@PathVariable UUID projectId,
                                                        @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, pageable));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable UUID projectId,
                                                     @Valid @RequestBody CreateTaskRequest request,
                                                     @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request, userId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID projectId,
                                                     @PathVariable UUID taskId,
                                                     @Valid @RequestBody UpdateTaskRequest request,
                                                     @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request, userId));
    }

    @PatchMapping("/position")
    public ResponseEntity<Void> updatePosition(@PathVariable UUID projectId,
                                                 @Valid @RequestBody UpdateTaskPositionRequest request,
                                                 @AuthenticationPrincipal UUID userId) {
        taskService.updateTaskPosition(projectId, request, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID projectId,
                                            @PathVariable UUID taskId,
                                            @AuthenticationPrincipal UUID userId) {
        taskService.deleteTask(projectId, taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
