package com.taskforge.task.service;

import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.collaborator.service.CollaboratorService;
import com.taskforge.label.service.LabelService;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
import com.taskforge.task.dto.CreateTaskRequest;
import com.taskforge.task.dto.UpdateTaskPositionRequest;
import com.taskforge.task.dto.UpdateTaskRequest;
import com.taskforge.task.dto.TaskResponse;
import com.taskforge.task.entity.Task;
import com.taskforge.task.entity.TaskStatus;
import com.taskforge.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private LabelService labelService;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private final UUID projectId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void getTasksByProject_shouldReturnOrderedTasks() {
        Task task = new Task("Test Task", "Desc", TaskStatus.TODO, 0, projectId, null);
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findByProjectId(eq(projectId), any(Pageable.class))).thenReturn(page);
        when(labelService.getTaskLabelIds(task.getId())).thenReturn(List.of());

        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, Pageable.ofSize(10));

        assertEquals(1, tasks.getTotalElements());
        assertEquals("Test Task", tasks.getContent().getFirst().getTitle());
    }

    @Test
    void createTask_shouldCreateWithAutoPosition() {
        when(projectRepository.existsById(projectId)).thenReturn(true);
        when(taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.TODO)).thenReturn(3);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");

        TaskResponse response = taskService.createTask(projectId, request, userId);

        assertEquals("New Task", response.getTitle());
        assertEquals(TaskStatus.TODO, response.getStatus());
        assertEquals(3, response.getPosition());
    }

    @Test
    void createTask_shouldThrowWhenProjectNotFound() {
        when(projectRepository.existsById(projectId)).thenReturn(false);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Task");

        assertThrows(ResponseStatusException.class, () -> taskService.createTask(projectId, request, userId));
    }

    @Test
    void updateTask_shouldModifyFields() {
        Task task = new Task("Old", "Old Desc", TaskStatus.TODO, 0, projectId, null);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("New Title");
        request.setDescription("New Desc");
        request.setStatus("IN_PROGRESS");

        TaskResponse response = taskService.updateTask(projectId, task.getId(), request, userId);

        assertEquals("New Title", response.getTitle());
        assertEquals("New Desc", response.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void updateTask_shouldThrowWhenInvalidStatus() {
        Task task = new Task("Old", "Desc", TaskStatus.TODO, 0, projectId, null);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus("INVALID");

        assertThrows(ResponseStatusException.class, () -> taskService.updateTask(projectId, task.getId(), request, userId));
    }

    @Test
    void updateTaskPosition_shouldRebalanceColumns() {
        Task task = new Task("Task", "Desc", TaskStatus.TODO, 0, projectId, null);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.findByProjectIdAndStatusOrderByPositionAsc(eq(projectId), any(TaskStatus.class)))
                .thenReturn(List.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateTaskPositionRequest request = new UpdateTaskPositionRequest();
        request.setTaskId(task.getId());
        request.setStatus("IN_PROGRESS");
        request.setPosition(0);

        assertDoesNotThrow(() -> taskService.updateTaskPosition(projectId, request, userId));
        verify(taskRepository, atLeastOnce()).save(any(Task.class));
    }

    @Test
    void deleteTask_shouldRemoveWhenOwner() {
        Task task = new Task("Task", "Desc", TaskStatus.TODO, 0, projectId, null);
        Project project = new Project("Project", "Desc", userId);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        taskService.deleteTask(projectId, task.getId(), userId);

        verify(taskRepository).deleteById(task.getId());
    }

    @Test
    void deleteTask_shouldThrowForbiddenWhenNotOwner() {
        Task task = new Task("Task", "Desc", TaskStatus.TODO, 0, projectId, null);
        Project project = new Project("Project", "Desc", userId);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class, () -> taskService.deleteTask(projectId, task.getId(), UUID.randomUUID()));
        verify(taskRepository, never()).deleteById(any());
    }
}
