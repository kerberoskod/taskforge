package com.taskforge.collaborator.repository;

import com.taskforge.collaborator.entity.ProjectCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectCollaboratorRepository extends JpaRepository<ProjectCollaborator, UUID> {
    List<ProjectCollaborator> findByProjectId(UUID projectId);
    List<ProjectCollaborator> findByUserId(UUID userId);
    Optional<ProjectCollaborator> findByProjectIdAndUserId(UUID projectId, UUID userId);
    boolean existsByProjectIdAndUserId(UUID projectId, UUID userId);
    void deleteByProjectIdAndUserId(UUID projectId, UUID userId);
}
