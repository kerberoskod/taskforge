package com.taskforge.label.controller;

import com.taskforge.label.dto.CreateLabelRequest;
import com.taskforge.label.dto.LabelResponse;
import com.taskforge.label.dto.SetTaskLabelsRequest;
import com.taskforge.label.service.LabelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/labels")
public class LabelController {

    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @GetMapping
    public ResponseEntity<List<LabelResponse>> getLabels(@PathVariable UUID projectId) {
        return ResponseEntity.ok(labelService.getLabelsByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<LabelResponse> createLabel(@PathVariable UUID projectId, @RequestBody CreateLabelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(labelService.createLabel(projectId, request));
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(@PathVariable UUID labelId) {
        labelService.deleteLabel(labelId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Void> setTaskLabels(@PathVariable UUID taskId, @RequestBody SetTaskLabelsRequest request) {
        labelService.setTaskLabels(taskId, request.getLabelIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<List<UUID>> getTaskLabels(@PathVariable UUID taskId) {
        return ResponseEntity.ok(labelService.getTaskLabelIds(taskId));
    }
}
