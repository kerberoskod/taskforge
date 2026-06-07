package com.taskforge.label.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    public Label() {
    }

    public Label(String name, String color, UUID projectId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.color = color;
        this.projectId = projectId;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public UUID getProjectId() { return projectId; }

    public void setName(String name) { this.name = name; }
    public void setColor(String color) { this.color = color; }
}
