package com.taskforge.comment.dto;

import com.taskforge.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommentResponse {

    private UUID id;
    private String content;
    private UUID taskId;
    private UUID authorId;
    private String authorName;
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.taskId = comment.getTaskId();
        this.authorId = comment.getAuthorId();
        this.authorName = comment.getAuthorName();
        this.createdAt = comment.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
