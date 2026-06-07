package com.taskforge.comment.service;

import com.taskforge.auth.entity.User;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.comment.dto.CommentRequest;
import com.taskforge.comment.dto.CommentResponse;
import com.taskforge.comment.entity.Comment;
import com.taskforge.comment.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public List<CommentResponse> getCommentsByTask(UUID taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)
                .stream()
                .map(CommentResponse::new)
                .toList();
    }

    public CommentResponse createComment(UUID taskId, CommentRequest request, UUID authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Comment comment = new Comment(request.getContent(), taskId, authorId, author.getName());
        commentRepository.save(comment);
        return new CommentResponse(comment);
    }

    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your comment");
        }

        commentRepository.delete(comment);
    }
}
