package com.taskforge.label.repository;

import com.taskforge.label.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findByProjectIdOrderByNameAsc(UUID projectId);
    List<Label> findByIdIn(List<UUID> ids);
}
