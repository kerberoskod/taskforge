package com.taskforge.project.repository;

import com.taskforge.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Page<Project> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.ownerId = :userId OR p.id IN " +
           "(SELECT c.projectId FROM ProjectCollaborator c WHERE c.userId = :userId) " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findAccessibleProjects(@Param("userId") UUID userId, Pageable pageable);
}
