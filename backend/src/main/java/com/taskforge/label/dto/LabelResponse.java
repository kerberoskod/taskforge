package com.taskforge.label.dto;

import com.taskforge.label.entity.Label;

import java.util.UUID;

public class LabelResponse {

    private UUID id;
    private String name;
    private String color;
    private UUID projectId;

    public LabelResponse(Label label) {
        this.id = label.getId();
        this.name = label.getName();
        this.color = label.getColor();
        this.projectId = label.getProjectId();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public UUID getProjectId() { return projectId; }
}
