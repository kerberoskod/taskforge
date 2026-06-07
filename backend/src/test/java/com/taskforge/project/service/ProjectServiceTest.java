package com.taskforge.project.service;

import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.collaborator.service.CollaboratorService;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.project.dto.CreateProjectRequest;
import com.taskforge.project.dto.ProjectResponse;
import com.taskforge.project.entity.Project;
import com.taskforge.project.repository.ProjectRepository;
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
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private final UUID ownerId = UUID.randomUUID();

    @Test
    void create_shouldSaveAndReturnProject() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project");
        request.setDescription("Description");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.create(request, ownerId);

        assertEquals("Test Project", response.getName());
        assertEquals("Description", response.getDescription());
        assertEquals(ownerId, response.getOwnerId());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void getProjectsByOwner_shouldReturnOwnedProjects() {
        Project project = new Project("P1", "Desc", ownerId);
        Page<Project> page = new PageImpl<>(List.of(project));
        when(projectRepository.findAccessibleProjects(eq(ownerId), any(Pageable.class))).thenReturn(page);

        Page<ProjectResponse> projects = projectService.getProjectsByOwner(ownerId, Pageable.ofSize(10));

        assertEquals(1, projects.getTotalElements());
        assertEquals("P1", projects.getContent().getFirst().getName());
    }

    @Test
    void getProject_shouldReturnProjectWhenOwner() {
        Project project = new Project("P1", "Desc", ownerId);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProject(project.getId(), ownerId);

        assertEquals("P1", response.getName());
    }

    @Test
    void getProject_shouldThrowNotFound() {
        when(projectRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProject(UUID.randomUUID(), ownerId));
    }

    @Test
    void getProject_shouldThrowForbiddenWhenNotOwner() {
        Project project = new Project("P1", "Desc", ownerId);
        UUID otherUser = UUID.randomUUID();
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class, () -> projectService.getProject(project.getId(), otherUser));
    }

    @Test
    void update_shouldModifyProject() {
        Project project = new Project("Old", "Old Desc", ownerId);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Name");
        request.setDescription("New Desc");

        ProjectResponse response = projectService.update(project.getId(), request, ownerId);

        assertEquals("New Name", response.getName());
        assertEquals("New Desc", response.getDescription());
    }

    @Test
    void delete_shouldRemoveProjectWhenOwner() {
        Project project = new Project("P1", "Desc", ownerId);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        projectService.delete(project.getId(), ownerId);

        verify(projectRepository).deleteById(project.getId());
    }

    @Test
    void delete_shouldThrowForbiddenWhenNotOwner() {
        Project project = new Project("P1", "Desc", ownerId);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class, () -> projectService.delete(project.getId(), UUID.randomUUID()));
        verify(projectRepository, never()).deleteById(any());
    }
}
