import client from './client';

export interface Comment {
  id: string;
  content: string;
  taskId: string;
  authorId: string;
  authorName: string;
  createdAt: string;
}

export const getComments = (taskId: string) =>
  client.get<Comment[]>(`/tasks/${taskId}/comments`).then((r) => r.data);

export const createComment = (taskId: string, content: string) =>
  client.post<Comment>(`/tasks/${taskId}/comments`, { content }).then((r) => r.data);

export const deleteComment = (commentId: string) =>
  client.delete(`/comments/${commentId}`);
