import client from './client';

export interface Label {
  id: string;
  name: string;
  color: string;
  projectId: string;
}

export interface CreateLabelData {
  name: string;
  color: string;
}

export const getLabels = (projectId: string) =>
  client.get<Label[]>(`/projects/${projectId}/labels`).then((r) => r.data);

export const createLabel = (projectId: string, data: CreateLabelData) =>
  client.post<Label>(`/projects/${projectId}/labels`, data).then((r) => r.data);

export const deleteLabel = (projectId: string, labelId: string) =>
  client.delete(`/projects/${projectId}/labels/${labelId}`);

export const setTaskLabels = (projectId: string, taskId: string, labelIds: string[]) =>
  client.put(`/projects/${projectId}/labels/tasks/${taskId}`, { labelIds });
