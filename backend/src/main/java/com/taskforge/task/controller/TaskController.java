package com.taskforge.task.controller;

import com.taskforge.task.dto.*;
import com.taskforge.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable UUID projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable UUID projectId,
                                                    @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(projectId, request));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID projectId,
                                                    @PathVariable UUID taskId,
                                                    @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request));
    }

    @PatchMapping("/position")
    public ResponseEntity<Void> updatePosition(@PathVariable UUID projectId,
                                                @Valid @RequestBody UpdateTaskPositionRequest request) {
        taskService.updateTaskPosition(projectId, request);
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
