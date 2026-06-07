package com.taskforge.task.service;

import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.collaborator.service.CollaboratorService;
import com.taskforge.label.service.LabelService;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
import com.taskforge.task.dto.*;
import com.taskforge.task.entity.Task;
import com.taskforge.task.entity.TaskStatus;
import com.taskforge.task.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final LabelService labelService;
    private final CollaboratorService collaboratorService;
    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       LabelService labelService, CollaboratorService collaboratorService,
                       ActivityLogService activityLogService, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.labelService = labelService;
        this.collaboratorService = collaboratorService;
        this.activityLogService = activityLogService;
        this.userRepository = userRepository;
    }

    public Page<TaskResponse> getTasksByProject(UUID projectId, Pageable pageable) {
        return taskRepository.findByProjectId(projectId, pageable)
                .map(task -> {
                    TaskResponse response = new TaskResponse(task);
                    response.setLabelIds(labelService.getTaskLabelIds(task.getId()));
                    return response;
                });
    }

    public TaskResponse createTask(UUID projectId, CreateTaskRequest request, UUID userId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
        }
        TaskStatus status = TaskStatus.TODO;
        if (request.getStatus() != null) {
            try {
                status = TaskStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus());
            }
        }

        int position = taskRepository.countByProjectIdAndStatus(projectId, status);

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                status,
                position,
                projectId,
                request.getAssigneeId()
        );
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        taskRepository.save(task);

        userRepository.findById(userId).ifPresent(user ->
                activityLogService.log(projectId, userId, user.getName(), "CREATED", "TASK", task.getId(),
                        "Created task: " + task.getTitle()));

        return new TaskResponse(task);
    }

    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            try {
                task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus());
            }
        }
        if (request.getAssigneeId() != null) {
            task.setAssigneeId(request.getAssigneeId());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        userRepository.findById(userId).ifPresent(user ->
                activityLogService.log(projectId, userId, user.getName(), "UPDATED", "TASK", taskId,
                        "Updated task: " + task.getTitle()));

        return new TaskResponse(task);
    }

    @Transactional
    public void updateTaskPosition(UUID projectId, UpdateTaskPositionRequest request, UUID userId) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = TaskStatus.valueOf(request.getStatus().toUpperCase());
        task.setStatus(newStatus);
        task.setPosition(request.getPosition());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        rebalanceColumn(projectId, newStatus);
        if (oldStatus != newStatus) {
            rebalanceColumn(projectId, oldStatus);
        }

        userRepository.findById(userId).ifPresent(user ->
                activityLogService.log(projectId, userId, user.getName(), "MOVED", "TASK", request.getTaskId(),
                        "Moved task from " + oldStatus + " to " + newStatus));
    }

    private void rebalanceColumn(UUID projectId, TaskStatus status) {
        List<Task> tasks = taskRepository.findByProjectIdAndStatusOrderByPositionAsc(projectId, status);
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }
        taskRepository.saveAll(tasks);
    }

    public void deleteTask(UUID projectId, UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not belong to this project");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!project.getOwnerId().equals(userId) && !collaboratorService.isCollaborator(projectId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete tasks in this project");
        }

        String taskTitle = task.getTitle();
        taskRepository.deleteById(taskId);

        userRepository.findById(userId).ifPresent(user ->
                activityLogService.log(projectId, userId, user.getName(), "DELETED", "TASK", taskId,
                        "Deleted task: " + taskTitle));
    }
}
