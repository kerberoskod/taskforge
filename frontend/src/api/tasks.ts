import client from './client';
import type { PageResponse } from './projects';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';

export interface Task {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  position: number;
  projectId: string;
  assigneeId: string | null;
  dueDate: string | null;
  labelIds: string[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskData {
  title: string;
  description?: string;
  status?: string;
  assigneeId?: string;
  dueDate?: string;
}

export interface UpdateTaskData {
  title?: string;
  description?: string;
  status?: string;
  assigneeId?: string;
  dueDate?: string;
}

export interface PositionData {
  taskId: string;
  status: string;
  position: number;
}

export const getTasks = (projectId: string, page = 0, size = 100) =>
  client.get<PageResponse<Task>>(`/projects/${projectId}/tasks`, { params: { page, size } }).then((r) => r.data);

export const createTask = (projectId: string, data: CreateTaskData) =>
  client.post<Task>(`/projects/${projectId}/tasks`, data).then((r) => r.data);

export const updateTask = (projectId: string, taskId: string, data: UpdateTaskData) =>
  client.put<Task>(`/projects/${projectId}/tasks/${taskId}`, data).then((r) => r.data);

export const updateTaskPosition = (projectId: string, data: PositionData) =>
  client.patch(`/projects/${projectId}/tasks/position`, data);

export const deleteTask = (projectId: string, taskId: string) =>
  client.delete(`/projects/${projectId}/tasks/${taskId}`);
