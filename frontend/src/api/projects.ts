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

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const getProjects = (page = 0, size = 12) =>
  client.get<PageResponse<Project>>('/projects', { params: { page, size } }).then((r) => r.data);

export const getProject = (id: string) =>
  client.get<Project>(`/projects/${id}`).then((r) => r.data);

export const createProject = (data: CreateProjectData) =>
  client.post<Project>('/projects', data).then((r) => r.data);

export const updateProject = (id: string, data: CreateProjectData) =>
  client.put<Project>(`/projects/${id}`, data).then((r) => r.data);

export const deleteProject = (id: string) =>
  client.delete(`/projects/${id}`);
