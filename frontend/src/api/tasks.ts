import client from './client';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';

export interface Task {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  position: number;
  projectId: string;
  assigneeId: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskData {
  title: string;
  description?: string;
  status?: string;
  assigneeId?: string;
}

export interface UpdateTaskData {
  title?: string;
  description?: string;
  status?: string;
  assigneeId?: string;
}

export interface PositionData {
  taskId: string;
  status: string;
  position: number;
}

export const getTasks = (projectId: string) =>
  client.get<Task[]>(`/projects/${projectId}/tasks`).then((r) => r.data);

export const createTask = (projectId: string, data: CreateTaskData) =>
  client.post<Task>(`/projects/${projectId}/tasks`, data).then((r) => r.data);

export const updateTask = (projectId: string, taskId: string, data: UpdateTaskData) =>
  client.put<Task>(`/projects/${projectId}/tasks/${taskId}`, data).then((r) => r.data);

export const updateTaskPosition = (projectId: string, data: PositionData) =>
  client.patch(`/projects/${projectId}/tasks/position`, data);

export const deleteTask = (projectId: string, taskId: string) =>
  client.delete(`/projects/${projectId}/tasks/${taskId}`);
