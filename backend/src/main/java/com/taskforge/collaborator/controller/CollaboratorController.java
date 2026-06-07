package com.taskforge.collaborator.controller;

import com.taskforge.collaborator.dto.CollaboratorRequest;
import com.taskforge.collaborator.dto.CollaboratorResponse;
import com.taskforge.collaborator.service.CollaboratorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/collaborators")
public class CollaboratorController {

    private final CollaboratorService collaboratorService;

    public CollaboratorController(CollaboratorService collaboratorService) {
        this.collaboratorService = collaboratorService;
    }

    @GetMapping
    public ResponseEntity<List<CollaboratorResponse>> getCollaborators(@PathVariable UUID projectId,
                                                                        @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(collaboratorService.getCollaborators(projectId, userId));
    }

    @PostMapping
    public ResponseEntity<CollaboratorResponse> addCollaborator(@PathVariable UUID projectId,
                                                                  @Valid @RequestBody CollaboratorRequest request,
                                                                  @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collaboratorService.addCollaborator(projectId, request, userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeCollaborator(@PathVariable UUID projectId,
                                                    @PathVariable UUID userId,
                                                    @AuthenticationPrincipal UUID ownerId) {
        collaboratorService.removeCollaborator(projectId, userId, ownerId);
        return ResponseEntity.noContent().build();
    }
}
