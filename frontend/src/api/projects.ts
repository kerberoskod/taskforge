import client from './client';

export interface Project {
  id: string;
  name: string;
  description: string;
  ownerId: string;
  createdAt: string;
}

export interface CreateProjectData {
  name: string;
  description?: string;
}

export const getProjects = () =>
  client.get<Project[]>('/projects').then((r) => r.data);

export const getProject = (id: string) =>
  client.get<Project>(`/projects/${id}`).then((r) => r.data);

export const createProject = (data: CreateProjectData) =>
  client.post<Project>('/projects', data).then((r) => r.data);

export const updateProject = (id: string, data: CreateProjectData) =>
  client.put<Project>(`/projects/${id}`, data).then((r) => r.data);

export const deleteProject = (id: string) =>
  client.delete(`/projects/${id}`);
