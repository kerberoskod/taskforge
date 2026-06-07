package com.taskforge.websocket;

import com.taskforge.task.dto.TaskResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BoardEventController {

    @MessageMapping("/board.move/{projectId}")
    @SendTo("/topic/projects/{projectId}")
    public TaskResponse onTaskMoved(@DestinationVariable String projectId, TaskResponse task) {
        return task;
    }

    @MessageMapping("/board.create/{projectId}")
    @SendTo("/topic/projects/{projectId}")
    public TaskResponse onTaskCreated(@DestinationVariable String projectId, TaskResponse task) {
        return task;
    }

    @MessageMapping("/board.update/{projectId}")
    @SendTo("/topic/projects/{projectId}")
    public TaskResponse onTaskUpdated(@DestinationVariable String projectId, TaskResponse task) {
        return task;
    }

    @MessageMapping("/board.delete/{projectId}")
    @SendTo("/topic/projects/{projectId}")
    public String onTaskDeleted(@DestinationVariable String projectId, String taskId) {
        return taskId;
    }
}
