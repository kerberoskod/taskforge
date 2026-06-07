import client from './client';

export interface Collaborator {
  id: string;
  projectId: string;
  userId: string;
  userName: string;
  userEmail: string;
  role: string;
  createdAt: string;
}

export interface AddCollaboratorData {
  userId: string;
  role?: string;
}

export const getCollaborators = (projectId: string) =>
  client.get<Collaborator[]>(`/projects/${projectId}/collaborators`).then((r) => r.data);

export const addCollaborator = (projectId: string, data: AddCollaboratorData) =>
  client.post<Collaborator>(`/projects/${projectId}/collaborators`, data).then((r) => r.data);

export const removeCollaborator = (projectId: string, userId: string) =>
  client.delete(`/projects/${projectId}/collaborators/${userId}`);
