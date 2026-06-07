package com.taskforge.task.repository;

import com.taskforge.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectIdOrderByPositionAsc(UUID projectId);
    int countByProjectIdAndStatus(UUID projectId, com.taskforge.task.entity.TaskStatus status);
    List<Task> findByProjectIdAndStatus(UUID projectId, com.taskforge.task.entity.TaskStatus status);
    List<Task> findByProjectIdAndStatusOrderByPositionAsc(UUID projectId, com.taskforge.task.entity.TaskStatus status);
}
