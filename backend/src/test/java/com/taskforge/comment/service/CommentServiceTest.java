package com.taskforge.comment.service;

import com.taskforge.activity.service.ActivityLogService;
import com.taskforge.auth.entity.User;
import com.taskforge.auth.repository.UserRepository;
import com.taskforge.comment.dto.CommentRequest;
import com.taskforge.comment.dto.CommentResponse;
import com.taskforge.comment.entity.Comment;
import com.taskforge.comment.repository.CommentRepository;
import com.taskforge.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CommentService commentService;

    private final UUID taskId = UUID.randomUUID();
    private final UUID authorId = UUID.randomUUID();

    @Test
    void getCommentsByTask_shouldReturnOrderedComments() {
        Comment comment = new Comment("Hello", taskId, authorId, "Test User");
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)).thenReturn(List.of(comment));

        List<CommentResponse> comments = commentService.getCommentsByTask(taskId);

        assertEquals(1, comments.size());
        assertEquals("Hello", comments.getFirst().getContent());
    }

    @Test
    void createComment_shouldSaveAndReturn() {
        CommentRequest request = new CommentRequest();
        request.setContent("New comment");

        User user = new User("Test User", "test@test.com", "pwd");
        when(userRepository.findById(authorId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response = commentService.createComment(taskId, request, authorId);

        assertEquals("New comment", response.getContent());
        assertEquals("Test User", response.getAuthorName());
    }

    @Test
    void createComment_shouldThrowWhenUserNotFound() {
        CommentRequest request = new CommentRequest();
        request.setContent("Comment");

        when(userRepository.findById(authorId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> commentService.createComment(taskId, request, authorId));
    }

    @Test
    void deleteComment_shouldRemoveWhenAuthor() {
        Comment comment = new Comment("Content", taskId, authorId, "User");
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(comment.getId(), authorId);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_shouldThrowForbiddenWhenNotAuthor() {
        Comment comment = new Comment("Content", taskId, authorId, "User");
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        assertThrows(ResponseStatusException.class, () -> commentService.deleteComment(comment.getId(), UUID.randomUUID()));
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_shouldThrowNotFound() {
        when(commentRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> commentService.deleteComment(UUID.randomUUID(), authorId));
    }
}
