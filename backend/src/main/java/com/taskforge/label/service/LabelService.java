package com.taskforge.label.service;

import com.taskforge.label.dto.CreateLabelRequest;
import com.taskforge.label.dto.LabelResponse;
import com.taskforge.label.entity.Label;
import com.taskforge.label.repository.LabelRepository;
import com.taskforge.task.entity.Task;
import com.taskforge.task.repository.TaskRepository;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class LabelService {

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final EntityManager entityManager;

    public LabelService(LabelRepository labelRepository, TaskRepository taskRepository, EntityManager entityManager) {
        this.labelRepository = labelRepository;
        this.taskRepository = taskRepository;
        this.entityManager = entityManager;
    }

    public List<LabelResponse> getLabelsByProject(UUID projectId) {
        return labelRepository.findByProjectIdOrderByNameAsc(projectId)
                .stream().map(LabelResponse::new).toList();
    }

    public LabelResponse createLabel(UUID projectId, CreateLabelRequest request) {
        Label label = new Label(request.getName(), request.getColor(), projectId);
        labelRepository.save(label);
        return new LabelResponse(label);
    }

    public void deleteLabel(UUID labelId) {
        if (!labelRepository.existsById(labelId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Label not found");
        }
        entityManager.createNativeQuery("DELETE FROM task_labels WHERE label_id = :id")
                .setParameter("id", labelId)
                .executeUpdate();
        labelRepository.deleteById(labelId);
    }

    @Transactional
    public void setTaskLabels(UUID taskId, List<UUID> labelIds) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }
        entityManager.createNativeQuery("DELETE FROM task_labels WHERE task_id = :taskId")
                .setParameter("taskId", taskId)
                .executeUpdate();
        for (UUID labelId : labelIds) {
            entityManager.createNativeQuery("INSERT INTO task_labels (task_id, label_id) VALUES (:taskId, :labelId)")
                    .setParameter("taskId", taskId)
                    .setParameter("labelId", labelId)
                    .executeUpdate();
        }
    }

    @SuppressWarnings("unchecked")
    public List<UUID> getTaskLabelIds(UUID taskId) {
        return entityManager.createNativeQuery("SELECT label_id FROM task_labels WHERE task_id = :taskId ORDER BY label_id")
                .setParameter("taskId", taskId)
                .getResultList()
                .stream()
                .map(id -> UUID.fromString(id.toString()))
                .toList();
    }
}
