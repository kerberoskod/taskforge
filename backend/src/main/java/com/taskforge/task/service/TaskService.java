package com.taskforge.task.service;

import com.taskforge.task.dto.*;
import com.taskforge.task.entity.Task;
import com.taskforge.task.entity.TaskStatus;
import com.taskforge.task.repository.TaskRepository;
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

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getTasksByProject(UUID projectId) {
        return taskRepository.findByProjectIdOrderByPositionAsc(projectId)
                .stream()
                .map(TaskResponse::new)
                .toList();
    }

    public TaskResponse createTask(UUID projectId, CreateTaskRequest request) {
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
        taskRepository.save(task);
        return new TaskResponse(task);
    }

    public TaskResponse updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request) {
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
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        return new TaskResponse(task);
    }

    @Transactional
    public void updateTaskPosition(UUID projectId, UpdateTaskPositionRequest request) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        TaskStatus newStatus = TaskStatus.valueOf(request.getStatus().toUpperCase());
        task.setStatus(newStatus);
        task.setPosition(request.getPosition());
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        List<Task> allTasks = taskRepository.findByProjectIdAndStatusOrderByPositionAsc(projectId, newStatus);
        for (int i = 0; i < allTasks.size(); i++) {
            allTasks.get(i).setPosition(i);
        }
        taskRepository.saveAll(allTasks);
    }

    public void deleteTask(UUID projectId, UUID taskId, UUID userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!task.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task does not belong to this project");
        }
        taskRepository.deleteById(taskId);
    }
}
