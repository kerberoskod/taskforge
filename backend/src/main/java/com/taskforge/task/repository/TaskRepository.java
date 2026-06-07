package com.taskforge.task.repository;

import com.taskforge.task.entity.Task;
import com.taskforge.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByProjectIdOrderByPositionAsc(UUID projectId, Pageable pageable);
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);
    int countByProjectIdAndStatus(UUID projectId, TaskStatus status);
    List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);
    List<Task> findByProjectIdAndStatusOrderByPositionAsc(UUID projectId, TaskStatus status);
}
